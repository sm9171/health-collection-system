package com.healthdata.health.adapter.in.batch;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@DisplayName("BatchScheduler 단위 테스트")
class BatchSchedulerTest {

    @Test
    @DisplayName("runTestAggregation을 수동으로 실행하면 JobLauncher를 호출한다")
    void runTestAggregation() throws Exception {
        // given
        JobLauncher mockJobLauncher = mock(JobLauncher.class);
        Job mockDailyJob = mock(Job.class);
        Job mockMonthlyJob = mock(Job.class);
        BatchScheduler batchScheduler = new BatchScheduler(mockJobLauncher, mockDailyJob, mockMonthlyJob);

        // when
        batchScheduler.runTestAggregation();

        // then
        ArgumentCaptor<JobParameters> paramsCaptor = ArgumentCaptor.forClass(JobParameters.class);
        verify(mockJobLauncher, times(1)).run(eq(mockDailyJob), paramsCaptor.capture());

        JobParameters capturedParams = paramsCaptor.getValue();
        assertThat(capturedParams.getParameters()).containsKey("runTime");
    }

    @Test
    @DisplayName("runTestAggregation 실행 중 예외가 발생해도 프로그램이 종료되지 않는다")
    void runTestAggregation_withException() throws Exception {
        // given
        JobLauncher mockJobLauncher = mock(JobLauncher.class);
        Job mockDailyJob = mock(Job.class);
        Job mockMonthlyJob = mock(Job.class);
        when(mockJobLauncher.run(any(), any())).thenThrow(new RuntimeException("Test exception"));

        BatchScheduler batchScheduler = new BatchScheduler(mockJobLauncher, mockDailyJob, mockMonthlyJob);

        // when & then
        assertThatCode(() -> batchScheduler.runTestAggregation())
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("runDailyAggregation을 실행하면 JobLauncher를 호출한다")
    void runDailyAggregation() throws Exception {
        // given
        JobLauncher mockJobLauncher = mock(JobLauncher.class);
        Job mockDailyJob = mock(Job.class);
        Job mockMonthlyJob = mock(Job.class);
        BatchScheduler batchScheduler = new BatchScheduler(mockJobLauncher, mockDailyJob, mockMonthlyJob);

        // when
        batchScheduler.runDailyAggregation();

        // then
        verify(mockJobLauncher, times(1)).run(eq(mockDailyJob), any(JobParameters.class));
    }
}
