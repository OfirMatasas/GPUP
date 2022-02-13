package task;

import com.google.gson.Gson;
import http.HttpClientUtil;
import javafx.application.Platform;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import patterns.Patterns;

import java.io.IOException;
import java.util.Objects;

public class ExecutedTargetUpdates {
    private final String taskName;
    private final String username;
    private final String targetName;
    private String runtimeStatus;
    private String resultStatus;
    private long sleepingTime;
    private String taskLog;

    public ExecutedTargetUpdates(String taskName, String targetName, String username) {
        this.targetName = targetName;
        this.taskName = taskName;
        this.username = username;
        this.runtimeStatus = "In process";
        this.resultStatus = "Undefined";
    }

    //------------------------------------------------- Getters -------------------------------------------------//
    public String getTargetName() {
        return this.targetName;
    }

    public String getRuntimeStatus() {
        return this.runtimeStatus;
    }

    public String getResultStatus() {
        return this.resultStatus;
    }

    public String getTaskLog() {
        return this.taskLog;
    }

    public String getTaskName() {
        return this.taskName;
    }

    public String getUsername() {
        return this.username;
    }

    public long getTotalTimeSlept() {
        return this.sleepingTime;
    }

    //------------------------------------------------- Setters -------------------------------------------------//
    public void setRuntimeStatus(String runtimeStatus) {
        this.runtimeStatus = runtimeStatus;
        updateServerOnUpdate();
    }

    public void setResultStatus(String resultStatus) {
        this.resultStatus = resultStatus;
    }

    public void setSleepingTime(long sleepingTime) { this.sleepingTime = sleepingTime; }

    //------------------------------------------------- Methods -------------------------------------------------//
    public void taskStarted() {
        this.taskLog = "The worker " + this.username + " just started working on target " + this.targetName + "!";
        setRuntimeStatus("In process");
    }

    public void goingToSleep(long predictedSleepingTime)
    {
        this.taskLog = "The target " + this.targetName + " is going to sleep for " + predictedSleepingTime + "m/s";

        updateServerOnUpdate();
    }

    public void taskFinished(String resultStatus, long time) {
        this.taskLog = "The worker " + this.username + " just finished working on target " + this.targetName + "!\n";
        this.taskLog += "The result: " + resultStatus + ", total time: " + time + "m/s" ;

        setResultStatus(resultStatus);
        setRuntimeStatus("Finished");
    }

    private void updateServerOnUpdate() {
        String updatesAsString = new Gson().toJson(this, ExecutedTargetUpdates.class);
        RequestBody body = RequestBody.create(updatesAsString, MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(Patterns.TASK_UPDATE)
                .post(body).addHeader("executed-task-update", this.taskName)
                .addHeader("target", this.targetName)
                .addHeader("username", this.username)
                .build();

        HttpClientUtil.runAsyncWithRequest(request, new Callback() {
            @Override public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> System.out.println("Couldn't connect to server for updating " +
                        ExecutedTargetUpdates.this.taskName + " task - " + ExecutedTargetUpdates.this.targetName + "!"));
            }

            @Override public void onResponse(@NotNull Call call, @NotNull Response response) {
                if(response.code() < 200 || response.code() >= 300)
                {
                    String message = response.header("message");
                    Platform.runLater(() ->
                    {
                        System.out.println("Error on updating server on " + ExecutedTargetUpdates.this.taskName + " - " + ExecutedTargetUpdates.this.targetName + "!");
                        System.out.println("Message: " + message);
                    });

                }
                Objects.requireNonNull(response.body()).close();
            }
        });
    }
}
