package de.ketrwu.discordtemperature.module.netatmo

import de.ketrwu.discordtemperature.logger
import de.ketrwu.discordtemperature.service.TemperatureService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.net.URL
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


@RestController
@RequestMapping("/netatmo")
@ConditionalOnProperty(prefix = "application.temperature", name = ["netatmo.enabled"])
class NetatmoOAuthCallbackController {

    @Autowired
    private lateinit var temperatureService: TemperatureService

    companion object {
        private val LOG = logger()
    }

    @GetMapping
    fun signin(request: HttpServletRequest, servletResponse: HttpServletResponse) {
        servletResponse.setHeader(HttpHeaders.LOCATION, (temperatureService as NetatmoTemperatureService).getAuthorizeUrl(getRequestUrl(request, "/netatmo/callback")))
        servletResponse.status = HttpStatus.TEMPORARY_REDIRECT.value()
    }

    @GetMapping("/callback")
    fun callback(
        request: HttpServletRequest,
        @RequestParam(required = false) code: String?,
        @RequestParam(required = false) error: String?
    ) = if (code != null) {
        (temperatureService as NetatmoTemperatureService).requestAccessToken(getRequestUrl(request, "/netatmo/callback"), code)
        "Thanks you can close the tab now!"
    } else {
        LOG.error("Failed to authorize against Netatmo: $error")
        "There was an error, please try again!"
    }

    private fun getRequestUrl(request: HttpServletRequest, path: String) = URL(request.requestURL.toString())
        .let {
            val port = if (it.port == -1) "" else ":${it.port}"
            "${it.protocol}://${it.host}$port$path"
        }
}