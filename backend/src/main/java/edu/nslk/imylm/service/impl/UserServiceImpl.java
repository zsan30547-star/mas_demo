// /backend/src/main/java/edu/nslk/imylm/service/impl/UserServiceImpl.java
// 职责描述：用户业务实现

package edu.nslk.imylm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.nslk.imylm.entity.SysUser;
import edu.nslk.imylm.mapper.UserMapper;
import edu.nslk.imylm.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, SysUser> implements UserService {

    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public SysUser getByUsername(String username) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username);
        return baseMapper.selectOne(wrapper);
    }

    @Override
    public Long register(String username, String password, String email) {
        SysUser user = new SysUser();
        user.setUsername(username);
        // BCrypt 加密存储密码
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setStatus(1);
        baseMapper.insert(user);
        return user.getId();
    }

    @Override
    public boolean updateProfile(Long userId, String email) {
        LambdaUpdateWrapper<SysUser> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SysUser::getId, userId).set(SysUser::getEmail, email);
        return update(wrapper);
    }

    @Override
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        SysUser user = getById(userId);
        if (user == null || !passwordEncoder.matches(oldPassword, user.getPassword())) {
            return false;
        }
        LambdaUpdateWrapper<SysUser> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SysUser::getId, userId).set(SysUser::getPassword, passwordEncoder.encode(newPassword));
        return update(wrapper);
    }
}
