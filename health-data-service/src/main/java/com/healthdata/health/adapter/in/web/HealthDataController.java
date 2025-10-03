package com.healthdata.health.adapter.in.web;

import com.healthdata.health.application.port.in.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

import static org.springframework.format.annotation.DateTimeFormat.ISO;

@RestController
@RequestMapping("/health-data")
@RequiredArgsConstructor
@Tag(name = "Health Data", description = "건강 데이터 관리 API")
@SecurityRequirement(name = "Bearer Authentication")
public class HealthDataController {

    private final CollectHealthDataUseCase collectHealthDataUseCase;
    private final QueryHealthDataUseCase queryHealthDataUseCase;

    @PostMapping("/collect")
    @Operation(summary = "건강 데이터 수집", description = "사용자의 건강 활동 데이터를 수집합니다.")
    @ApiResponse(responseCode = "201", description = "데이터 수집 성공",
                content = @Content(schema = @Schema(implementation = CollectResponse.class)))
    @ApiResponse(responseCode = "400", description = "입력값 검증 실패")
    @ApiResponse(responseCode = "409", description = "이미 수집된 데이터")
    public ResponseEntity<CollectResponse> collectHealthData(
            @Parameter(hidden = true) @RequestHeader("X-User-Email") String userEmail,
            @Parameter(hidden = true) @RequestHeader("X-Record-Key") String recordKey,
            @Valid @RequestBody HealthDataCollectionRequest request) {

        CollectHealthDataCommand command = CollectHealthDataCommand.builder()
                .recordKey(recordKey)
                .entries(request.data().entries().stream()
                        .map(entry -> CollectHealthDataCommand.HealthDataEntry.builder()
                                .from(entry.period().from())
                                .to(entry.period().to())
                                .steps(entry.steps())
                                .caloriesValue(entry.calories().value())
                                .caloriesUnit(entry.calories().unit())
                                .distanceValue(entry.distance().value())
                                .distanceUnit(entry.distance().unit())
                                .build())
                        .toList())
                .build();

        collectHealthDataUseCase.collect(command);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CollectResponse("Health data collected successfully",
                                        request.data().entries().size()));
    }

    @GetMapping
    @Operation(summary = "건강 데이터 조회", description = "기간별 건강 활동 데이터를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "데이터 조회 성공", 
                content = @Content(schema = @Schema(implementation = HealthDataListResponse.class)))
    @ApiResponse(responseCode = "400", description = "입력값 검증 실패")
    public ResponseEntity<HealthDataListResponse> query(
            @Parameter(hidden = true) @RequestHeader("X-User-Email") String userEmail,
            @Parameter(description = "레코드 키", required = true) @RequestParam String recordKey,
            @Parameter(description = "조회 시작 시간 (ISO DateTime)", required = true) 
            @RequestParam @DateTimeFormat(iso = ISO.DATE_TIME) LocalDateTime from,
            @Parameter(description = "조회 종료 시간 (ISO DateTime)", required = true) 
            @RequestParam @DateTimeFormat(iso = ISO.DATE_TIME) LocalDateTime to) {
        
        QueryHealthDataCommand command = QueryHealthDataCommand.builder()
                .recordKey(recordKey)
                .from(from)
                .to(to)
                .build();
        
        HealthDataListResponse response = queryHealthDataUseCase.query(command);
        return ResponseEntity.ok(response);
    }
}