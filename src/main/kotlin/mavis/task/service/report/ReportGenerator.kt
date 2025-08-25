package mavis.task.service.report

import mavis.task.util.dto.DailyReport
import java.time.LocalDate

interface ReportGenerator {
    suspend fun generateDailyReport(date: LocalDate): DailyReport
}