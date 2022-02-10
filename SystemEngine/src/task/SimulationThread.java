package task;

import com.google.gson.Gson;
import http.HttpClientUtil;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import patterns.Patterns;
import summaries.TargetSummary;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class SimulationThread implements Runnable
{
    private final SimulationParameters targetParameters;
    private final ExecutedTargetUpdates updates;
    private final String targetName;
    private final String workerName;
    private final String taskName;
    private Duration predictedTime;
    private final Gson gson;

    public SimulationThread(WorkerSimulationParameters taskInfo) {
        this.targetParameters = taskInfo.getParameters();
        this.targetName = taskInfo.getTargetName();
        this.workerName = taskInfo.getWorkerName();
        this.taskName = taskInfo.getTaskName();
        this.gson = new Gson();
        this.updates = new ExecutedTargetUpdates(this.targetName);
        UpdateWorkingTime();
    }

    @Override public void run() {
        Thread.currentThread().setName(this.targetName + " Thread");
        long sleepingTime = this.targetParameters.getProcessingTime().toMillis();

        //Starting the clock
        this.updates.startTheClock();

        //Going to sleep
        try {
            Thread.sleep(this.predictedTime.toMillis());
        } catch (InterruptedException e) {
            try {
                Thread.sleep(sleepingTime - Duration.between(this.updates.getTimeStarted(), Instant.now()).toMillis());
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        finally
        {
            checkForSuccess();
        }
    }

    private void UpdateWorkingTime() {
        long timeLong;
        this.predictedTime = this.targetParameters.getProcessingTime();

        if(this.targetParameters.isRandom())
        {
            timeLong = (long)(Math.random() * (this.predictedTime.toMillis())) + 1;
            this.predictedTime = Duration.of(timeLong, ChronoUnit.MILLIS);
        }
    }

    private void checkForSuccess() {
        String resultStatus;
        double result = Math.random();
        if(Math.random() <= this.targetParameters.getSuccessRate())
            resultStatus = result <= this.targetParameters.getSuccessWithWarnings() ?
                    TargetSummary.ResultStatus.Warning.toString() : TargetSummary.ResultStatus.Success.toString();
        else
            resultStatus = TargetSummary.ResultStatus.Failure.toString();

        this.updates.stopTheClock();
        this.updates.setResultStatus(resultStatus);
    }

    private void updateServerOnUpdate()
    {
        String updatesAsString = this.gson.toJson(this.updates, ExecutedTargetUpdates.class);
        RequestBody body = RequestBody.create(updatesAsString, MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(Patterns.TASK_UPDATE)
                .post(body).addHeader("task-update", this.taskName)
                .addHeader("target", this.targetName)
                .addHeader("username", this.workerName)
                .build();

        HttpClientUtil.runAsyncWithRequest(request, new Callback() {
            @Override public void onFailure(@NotNull Call call, @NotNull IOException e) {
                System.out.println("Couldn't connect to server for updating " +
                        SimulationThread.this.taskName + " task - " + SimulationThread.this.targetName + "!");
            }

            @Override public void onResponse(@NotNull Call call, @NotNull Response response) {
                System.out.println("got task response - success");
                if(response.code() >= 200 && response.code() < 300)
                    System.out.println("Updated server on " + SimulationThread.this.taskName + " - " + SimulationThread.this.targetName + "!");
                else {

                    System.out.println("Error on updating server on " + SimulationThread.this.taskName + " - " + SimulationThread.this.targetName + "!");
                    System.out.println("Target name: " + SimulationThread.this.targetName);
                }
            }
        });
    }
}