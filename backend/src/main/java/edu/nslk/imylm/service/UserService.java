// /backend/src/main/java/edu/nslk/imylm/service/UserService.java
// 职责描述：用户业务接口

package edu.nslk.imylm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.nslk.imylm.entity.SysUser;

public interface UserService extends IService<SysUser> {

    // 根据用户名查询用户，返回值 SysUser
    SysUser getByUsername(String username);

    // 注册用户，返回值 注册后的用户ID
    Long register(String username, String password, String email);

    // 更新用户信息，返回 boolean
    boolean updateProfile(Long userId, String email);

    // 修改密码，返回 boolean（原密码错误返回 false）
    boolean changePassword(Long userId, String oldPassword, String newPassword);
}
