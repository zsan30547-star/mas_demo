// /backend/src/main/java/edu/nslk/imylm/service/impl/TaskServiceImpl.java
// 职责描述：任务业务实现

package edu.nslk.imylm.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.nslk.imylm.entity.Task;
import edu.nslk.imylm.mapper.TaskMapper;
import edu.nslk.imylm.service.TaskService;
import org.springframework.stereotype.Service;

@Service
public class TaskServiceImpl extends ServiceImpl<TaskMapper, Task> implements TaskService {
}
