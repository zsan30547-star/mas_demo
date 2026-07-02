// /backend/src/main/java/edu/nslk/imylm/service/AgentService.java
// 职责描述：Agent 配置业务接口

package edu.nslk.imylm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.nslk.imylm.entity.AgentConfig;

import java.util.List;

public interface AgentService extends IService<AgentConfig> {

    // 查询用户的所有 Agent 列表，返回值 List<AgentConfig>
    List<AgentConfig> getByUserId(Long userId);

    // 查询系统预置 Agent 列表，返回值 List<AgentConfig>
    List<AgentConfig> getPresets();
}
