package mavis.task.controller

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import mavis.task.service.report.ReportGenerator
import mavis.task.util.constants.ErrorMessages
import mavis.task.util.ServerSettings
import mavis.task.util.exception.ReportException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import java.time.LocalDate
import java.time.format.DateTimeParseException


@RestController
class DailyReportController(
    private val reportGenerator: ReportGenerator
) {
    private val log = LoggerFactory.getLogger(DailyReportController::class.java)

    @GetMapping(PATH_REPORT_DAILY)
    suspend fun dailyReport(@RequestParam(DATE) dateString: String?): Any {
        requireNotNull(dateString) { ErrorMessages.PARAMETER_NOT_PROVIDED.format(DATE) }

        return try {
            val date = LocalDate.parse(dateString, ServerSettings.DATE_TIME_FORMATTER)
            return reportGenerator.generateDailyReport(date)
        } catch (e: DateTimeParseException) {
            handleParsingError(e, dateString)
        } catch (e: ReportException) {
            handleGenerationError(e)
        } catch (e: Exception) {
            handleUnexpectedError(e)
        }
    }


    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(DateTimeParseException::class)
    fun handleParsingError(exception: DateTimeParseException, dateString: String): String {
        val errorMessage = ErrorMessages.ERROR_WHILE_PARSING.format(dateString, LocalDate::class.simpleName)
        log.error(errorMessage, exception)
        return errorMessage
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(ReportException::class)
    fun handleGenerationError(exception: ReportException): String {
        val errorMessage = ErrorMessages.CANNOT_GENERATE.format(REPORT, exception.message)
        log.error(errorMessage, exception)
        return errorMessage
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception::class)
    fun handleUnexpectedError(exception: Exception): String {
        log.error(ErrorMessages.UNEXPECTED_EXCEPTION, exception)
        return ErrorMessages.UNEXPECTED_EXCEPTION
    }

    companion object {
        private const val DATE = "date"
        private const val REPORT = "report"
        private const val PATH_REPORT_DAILY = "/report/daily"
    }
}