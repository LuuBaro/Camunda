package com.example.workflow.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.camunda.bpm.engine.task.Task;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskDTO {
    private String taskId;
    private String name;
    private String assignee;
    private String processInstanceId;

    public static TaskDTO fromTask(Task task) {
        return new TaskDTO(
                task.getId(),
                task.getName(),
                task.getAssignee(),
                task.getProcessInstanceId()
        );
    }
}