package esthesis.platform.backend.server.workflow;

import lombok.extern.java.Log;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Log
@Component
public class DebugTask implements JavaDelegate {

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    System.out.println("DEBUG!!!!!!!!!!!");
    System.out.println(execution.getVariable("loopCounter"));
  }
}
