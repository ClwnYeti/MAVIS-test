package mavis.task

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
open class MavisTestApplication {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<MavisTestApplication>(*args)
        }
    }
}