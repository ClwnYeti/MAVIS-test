package mavis.task.service.report

import org.slf4j.LoggerFactory
import mavis.task.util.ServerSettings
import mavis.task.util.constants.ErrorMessages
import mavis.task.util.dto.DailyReport
import mavis.task.util.dto.HamsterStats
import mavis.task.util.exception.ReportException
import org.springframework.stereotype.Service
import java.lang.String.format
import java.time.LocalDate

@Service
class ReportGeneratorImpl(
    private val dailyInfoCalculator: DailyInfoCalculator
) : ReportGenerator {
    private val log = LoggerFactory.getLogger(ReportGeneratorImpl::class.java)
    override suspend fun generateDailyReport(date: LocalDate): DailyReport {
        val currentDate = LocalDate.now()
        if (date > currentDate) {
            val formatedMessage =
                format(ErrorMessages.DATE_IS_NOT_STARTED_FOR_REPORT, date.format(ServerSettings.DATE_TIME_FORMATTER))
            log.error(formatedMessage)
            throw ReportException(formatedMessage)
        }
        return DailyReport(date, dailyInfoCalculator.getStatByDate(date).map {
            HamsterStats(it.id, it.totalRounds, it.dateRounds >= 10)
        })
    }
}