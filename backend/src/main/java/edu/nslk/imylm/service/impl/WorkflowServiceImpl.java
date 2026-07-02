// /backend/src/main/java/edu/nslk/imylm/service/impl/WorkflowServiceImpl.java
// 职责描述：工作流模板业务实现

package edu.nslk.imylm.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.nslk.imylm.entity.WorkflowTemplate;
import edu.nslk.imylm.mapper.WorkflowTemplateMapper;
import edu.nslk.imylm.service.WorkflowService;
import org.springframework.stereotype.Service;

@Service
public class WorkflowServiceImpl extends ServiceImpl<WorkflowTemplateMapper, WorkflowTemplate> implements WorkflowService {
}
