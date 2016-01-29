import org.springframework.web.bind.annotation.PathVariable

import java.util.logging.Logger

@RestController
class LoggingRestController {

    @RequestMapping("/{msg}")
    String log(@PathVariable String msg) {
        Logger logger = Logger.getLogger("TEST")
        logger.info ("test: " + msg )
        return msg
    }
}
