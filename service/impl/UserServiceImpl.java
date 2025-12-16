package cn.edu.chtholly.service.impl;

import cn.edu.chtholly.dao.UserDao;
import cn.edu.chtholly.dao.impl.UserDaoImpl;
import cn.edu.chtholly.model.Result;
import cn.edu.chtholly.model.param.UserQueryParam;
import cn.edu.chtholly.model.vo.*;
import cn.edu.chtholly.util.*;
import cn.edu.chtholly.model.entity.User;
import cn.edu.chtholly.service.UserService;

import static cn.edu.chtholly.util.EncryptUtil.md5Encrypt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.Part;
import java.sql.Timestamp;

public class UserServiceImpl implements UserService {

    private final UserDao userDao = new UserDaoImpl();

    @Override
    public Result<String> login(LoginVO loginVO) {
        String email = loginVO.getEmail();
        String password = loginVO.getPassword();

        // 1. 校验邮箱格式
        if (email == null || !email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$")) {
            return Result.error("邮箱格式无效");
        }

        // 2. 校验密码不为空
        if (password == null || password.trim().isEmpty()) {
            return Result.error("密码不能为空");
        }

        // 3. 根据邮箱查询用户
        User user = userDao.selectByEmail(email);
        if (user == null) {
            return Result.error("邮箱不存在");
        }

        // 4. 校验账号状态（0-启用，1-禁止）
        if (user.getStatus() != 0) {
            return Result.error("账号已被禁止登录");
        }

        // 5. 密码MD5加密
        String encryptedPassword = md5Encrypt(password);
        if (!encryptedPassword.equals(user.getPassword())) {
            return Result.error("密码错误");
        }

        // 6. 生成JWT令牌（HMAC256签名）
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());       // 用户ID
        claims.put("username", user.getUsername()); // 用户名
        String token = JwtUtil.generateToken(claims);

        // 7. 返回成功响应
        return Result.success(token);
    }

    // 获取当前登录用户信息
    @Override
    public Result<UserInfoVO> getUserInfo() {
        // 1. 从ThreadLocal获取当前登录用户ID
        Long userId = UserThreadLocal.getUserId();
        if (userId == null) {
            return Result.error("未登录");
        }

        // 2. 根据用户ID查询数据库
        User user = userDao.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 3. 转换为VO
        UserInfoVO vo = new UserInfoVO();
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setUserAvatar(user.getUserAvatar());
        vo.setIntroduction(user.getIntroduction());

        // 4. 返回成功响应
        return Result.success(vo);
    }

    // 核心修改：复用接口，自动区分「注册/重置密码」场景
    @Override
    public Result<String> sendVerificationCode(String email) {
        // 1. 基础校验：邮箱非空+格式有效
        if (email == null || email.trim().isEmpty()) {
            return Result.error("邮箱不能为空");
        }
        email = email.trim();
        if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$")) {
            return Result.error("邮箱格式无效");
        }

        // 2. 判断场景：未注册→注册验证码，已注册→重置密码验证码
        boolean isRegistered = userDao.checkEmailExists(email);
        String code = VerificationCodeUtil.generateCode();
        String redisKey; // 区分Redis Key，避免覆盖
        boolean sendSuccess;

        if (!isRegistered) {
            // 场景1：未注册→注册验证码
            redisKey = "verify_code:register:" + email;
            sendSuccess = MailUtil.sendRegisterCode(email, code); // 需MailUtil有该方法
        } else {
            // 场景2：已注册→重置密码验证码
            redisKey = "verify_code:reset:" + email;
            sendSuccess = MailUtil.sendResetPwdCode(email, code); // 需MailUtil有该方法
        }

        // 3. 发送/存储失败处理
        if (!sendSuccess) {
            return Result.error("邮件发送失败，请稍后重试");
        }
        RedisUtil.setex(redisKey, code, 300); // 5分钟过期

        // 4. 统一响应格式：{"code":0,"message":"邮件发送成功","data":null}
        return Result.success(null, "邮件发送成功");
    }

    @Override
    public Result<String> register(RegisterVO registerVO) {
        // 1. 提取参数并校验基础合法性
        String username = registerVO.getUsername();
        String email = registerVO.getEmail();
        String password = registerVO.getPassword();
        String code = registerVO.getVerificationCode();

        // 用户名校验（2-20位）
        if (username == null || username.trim().length() < 2 || username.trim().length() > 20) {
            return Result.error("用户名必须为2-20位");
        }
        // 密码校验（6-20位）
        if (password == null || password.length() < 6 || password.length() > 20) {
            return Result.error("密码必须为6-20位");
        }
        if (email == null || !email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$")) {
            return Result.error("邮箱格式无效");
        }
        // 验证码校验（6位）
        if (code == null || code.length() != 6) {
            return Result.error("验证码必须为6位数字");
        }

        // 2. 校验「注册场景」的验证码
        String redisKey = "verify_code:register:" + email.trim();
        String storedCode = RedisUtil.get(redisKey);
        if (storedCode == null) {
            return Result.error("验证码已过期，请重新获取");
        }
        if (!storedCode.equals(code)) {
            return Result.error("验证码错误");
        }

        // 3. 检查邮箱是否已注册
        if (userDao.checkEmailExists(email)) {
            return Result.error("该邮箱已注册");
        }

        // 4. 密码加密
        String encryptedPwd = EncryptUtil.md5Encrypt(password);

        // 5. 构建User实体并保存到数据库
        User user = new User();
        user.setUsername(username.trim());
        user.setPassword(encryptedPwd);
        user.setEmail(email);
        int insertRows = userDao.insertUser(user);
        if (insertRows <= 0) {
            return Result.error("注册失败，请稍后重试");
        }

        // 6. 注册成功：删除Redis中的验证码
        RedisUtil.del(redisKey);

        // 7. 返回成功
        return Result.success(null, "注册成功");
    }

    @Override
    public Result<String> updateUserInfo(UpdateUserInfoVO vo) {
        // ① 登录校验（从ThreadLocal获取当前登录用户ID）
        Long loginUserId = UserThreadLocal.getUserId();
        if (loginUserId == null) {
            return Result.error("请先登录");
        }

        // ② 参数校验
        if (vo.getUserId() == null || vo.getUserId() <= 0) {
            return Result.error("用户ID无效");
        }
        // 权限校验：只能更新本人信息（请求的userId必须等于登录userId）
        if (!vo.getUserId().equals(loginUserId)) {
            return Result.error("无权限更新他人信息");
        }
        // 用户名校验（非空且长度≤20）
        if (vo.getUsername() != null && (vo.getUsername().trim().isEmpty() || vo.getUsername().length() > 20)) {
            return Result.error("用户名不能为空且长度不超过20字");
        }
        // 手机号校验（非空且为11位数字）
        if (vo.getPhone() != null) {
            String phone = vo.getPhone().trim();
            if (phone.isEmpty() || !phone.matches("^1[3-9]\\d{9}$")) {
                return Result.error("手机号格式无效（需为11位有效手机号）");
            }
        }
        // 邮箱校验（非空且格式正确）
        if (vo.getEmail() != null) {
            String email = vo.getEmail().trim();
            if (email.isEmpty() || !email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$")) {
                return Result.error("邮箱格式无效");
            }
            // 校验邮箱是否已被其他用户占用
            User existingUser = userDao.getUserByEmail(email);
            if (existingUser != null && !existingUser.getId().equals(loginUserId)) {
                return Result.error("该邮箱已被其他账号绑定");
            }
        }
        // 个人简介校验（长度≤255）
        if (vo.getIntroduction() != null && vo.getIntroduction().length() > 255) {
            return Result.error("个人简介长度不超过255字");
        }

        // ③ 构建User实体（只设置非null字段）
        User user = new User();
        user.setId(loginUserId);
        if (vo.getUsername() != null) user.setUsername(vo.getUsername().trim());
        if (vo.getPhone() != null) user.setPhone(vo.getPhone().trim());
        if (vo.getEmail() != null) user.setEmail(vo.getEmail().trim());
        if (vo.getIntroduction() != null) user.setIntroduction(vo.getIntroduction().trim());

        // ④ 执行更新
        int updateRows = userDao.updateUserInfo(user);
        if (updateRows <= 0) {
            return Result.error("更新信息失败，请稍后重试");
        }

        return Result.success(null, "更新成功");
    }

    // 删除账号
    @Override
    public Result<String> deleteAccount() {
        // ① 登录校验
        Long loginUserId = UserThreadLocal.getUserId();
        if (loginUserId == null) {
            return Result.error("请先登录");
        }

        // ② 执行删除
        int deleteRows = userDao.deleteUserById(loginUserId);
        if (deleteRows <= 0) {
            return Result.error("删除账号失败，请稍后重试");
        }

        return Result.success(null, "删除成功");
    }

    // 重置密码（校验「重置场景」的验证码，逻辑不变）
    @Override
    public Result<String> resetPassword(ResetPasswordVO vo) {
        // ① 参数校验
        if (vo.getEmail() == null || vo.getEmail().trim().isEmpty()) {
            return Result.error("邮箱不能为空");
        }
        String email = vo.getEmail().trim();
        if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$")) {
            return Result.error("邮箱格式无效");
        }
        if (vo.getVerificationCode() == null || vo.getVerificationCode().trim().isEmpty()) {
            return Result.error("验证码不能为空");
        }
        if (vo.getNewPassword() == null || vo.getNewPassword().trim().isEmpty()) {
            return Result.error("新密码不能为空");
        }
        if (vo.getRepeatPassword() == null || vo.getRepeatPassword().trim().isEmpty()) {
            return Result.error("请重复新密码");
        }

        // ② 密码一致性校验（长度≥6）
        String newPwd = vo.getNewPassword().trim();
        String repeatPwd = vo.getRepeatPassword().trim();
        if (!newPwd.equals(repeatPwd)) {
            return Result.error("两次输入的密码不一致");
        }
        if (newPwd.length() < 6) {
            return Result.error("新密码长度至少6位");
        }

        // ③ 校验邮箱是否存在
        User user = userDao.getUserByEmail(email);
        if (user == null) {
            return Result.error("该邮箱未绑定任何账号");
        }

        // ④ 校验「重置场景」的验证码（Key：verify_code:reset:邮箱）
        String redisKey = "verify_code:reset:" + email;
        String storedCode = RedisUtil.get(redisKey);
        if (storedCode == null) {
            return Result.error("验证码已过期，请重新获取");
        }
        if (!storedCode.equals(vo.getVerificationCode().trim())) {
            return Result.error("验证码错误");
        }

        // ⑤ 加密新密码
        String encryptedPwd = EncryptUtil.md5Encrypt(newPwd);
        user.setPassword(encryptedPwd);
        user.setUpdateTime(new java.util.Date());

        // ⑥ 执行密码更新
        int updateRows = userDao.updateUserInfo(user);
        if (updateRows <= 0) {
            return Result.error("密码重置失败，请稍后重试");
        }

        // ⑦ 删除Redis中的验证码
        RedisUtil.del(redisKey);

        return Result.success(null, "密码重置成功");
    }

    @Override
    public Result<String> updateUserAvatar(Long userId, Part avatarPart) {
        try {
            // 1. 业务校验：文件类型
            String contentType = avatarPart.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return Result.error("请上传图片类型文件");
            }

            // 2. 上传新头像到MinIO
            String newAvatarUrl = MinioUtil.uploadAvatar(avatarPart);

            // 3. 查询旧头像URL
            String oldAvatarUrl = userDao.queryUserAvatarById(userId);

            // 4. 删除旧头像
            if (oldAvatarUrl != null && !oldAvatarUrl.isEmpty()) {
                MinioUtil.deleteAvatar(oldAvatarUrl);
            }

            // 5. 更新数据库（业务层调用Dao）
            int rows = userDao.updateUserAvatar(
                    userId,
                    newAvatarUrl,
                    new Timestamp(System.currentTimeMillis())
            );

            if (rows > 0) {
                return Result.success(null, "更新成功");
            } else {
                return Result.error("更新失败");
            }

        } catch (Exception e) {
            return Result.error("更新头像失败：" + e.getMessage());
        }
    }

    @Override
    public Result<PageResultVO> getAllUsers(UserQueryParam param) {
        // 1. 处理分页默认值
        int pageNum = param.getPageNum() == null ? 1 : param.getPageNum();
        int pageSize = param.getPageSize() == null ? 15 : param.getPageSize();
        int offset = (pageNum - 1) * pageSize;

        // 2. 查询数据
        List<UserItemVO> items = userDao.selectUserPage(param, offset);
        Long total = userDao.selectUserTotal(param);

        // 3. 封装结果
        PageResultVO<UserItemVO> pageResult = new PageResultVO<>();
        pageResult.setTotal(total);
        pageResult.setItems(items);
        return Result.success(pageResult, "操作成功");
    }

    @Override
    public Result<Void> addUser(User user) {
        // 1. 参数校验
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            return Result.error("用户名不能为空");
        }
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            return Result.error("密码不能为空");
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            return Result.error("邮箱不能为空");
        }
        if (userDao.checkEmailExists(user.getEmail())) {
            return Result.error("邮箱已被注册");
        }

        // 2. 处理密码（MD5加密）
        user.setPassword(EncryptUtil.md5Encrypt(user.getPassword().trim()));

        // 3. 处理状态（前端传入的userStatus→数据库status：1→禁用，其他→启用）
        Integer status = user.getStatus() == null ? 0 : (user.getStatus() == 1 ? 1 : 0);
        user.setStatus(status);

        // 4. 执行新增
        int rows = userDao.insertUser(user);
        return rows > 0 ? Result.success(null, "添加成功") : Result.error("添加失败");
    }

    @Override
    public Result<Void> updateUser(User user) {
        System.out.println(user);
        // 1. 参数校验
        if (user.getId() == null) {
            return Result.error("用户ID不能为空");
        }
        User oldUser = userDao.selectById(user.getId());
        if (oldUser == null) {
            return Result.error("用户不存在");
        }

        // 2. 处理密码（为空则不更新，不为空则加密）
        if (user.getPassword() != null && !user.getPassword().trim().isEmpty()) {
            user.setPassword(EncryptUtil.md5Encrypt(user.getPassword().trim()));
        } else {
            user.setPassword(null); // 确保空密码不参与更新
        }

        // 3. 处理状态（前端userStatus→数据库status：0→启用，1→禁用）
        if (user.getStatus() != null) {
            user.setStatus(user.getStatus() == 0 ? 0 : 1);
        }

        // 4. 执行更新
        int rows = userDao.updateUserInfo(user);
        return rows > 0 ? Result.success(null, "更新成功") : Result.error("更新失败");
    }

    @Override
    public Result<Void> updateUserStatus(Long userId, Integer status) {
        // 1. 参数校验
        if (userId == null) {
            return Result.error("用户ID不能为空");
        }
        if (status == null || (status != 0 && status != 1)) {
            return Result.error("状态值必须为0（启用）或1（禁用）");
        }
        if (userDao.selectById(userId) == null) {
            return Result.error("用户不存在");
        }

        // 2. 执行状态更新
        int rows = userDao.updateUserStatus(userId, status);
        return rows > 0 ? Result.success(null, "状态更新成功") : Result.error("状态更新失败");
    }

    @Override
    public Result<Void> deleteUser(Long userId) {
        if (userId == null) {
            return Result.error("用户ID不能为空");
        }
        if (userDao.selectById(userId) == null) {
            return Result.error("用户不存在");
        }

        int rows = userDao.deleteUserById(userId);
        return rows > 0 ? Result.success(null, "删除成功") : Result.error("删除失败");
    }

    @Override
    public Result<Void> deleteUsers (List<Long> userIds) {
        // 1. 参数校验
        if (userIds == null || userIds.isEmpty()) {
            return Result.error("用户ID列表不能为空");
        }

        try {
            // 2. 校验所有用户是否存在（避免删除不存在的ID）
            for (Long userId : userIds) {
                User user = userDao.selectById(userId);
                if (user == null) {
                    return Result.error("用户ID " + userId + " 不存在，批量删除失败");
                }
            }

            // 3. 执行批量删除（调用DAO的批量删除方法）
            int rows = userDao.deleteUserByIds(userIds);
            if (rows != userIds.size()) {
                return Result.error("部分用户删除失败");
            }

            // 4. 返回符合要求的成功响应
            return Result.success(null, "删除成功");
        } catch (Exception e) {
            return Result.error("批量删除失败：" + e.getMessage());
        }
    }

}