package rs.lukaj.utils.webwatcher.surprise

import com.sendgrid.Email
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.context.request.ServletWebRequest
import rs.lukaj.utils.webwatcher.Executor
import java.io.File
import java.util.*
import kotlin.math.min

/**
 * Birthday surprise, a side project (needed this infrastructure for rapid development & deployment)
 */
@Controller
class SurpriseController {
    companion object {
        private const val ENABLED = false

        private fun isTurnedOn() : Boolean = ENABLED &&
            Calendar.getInstance()[Calendar.DAY_OF_MONTH] == 19 && Calendar.getInstance()[Calendar.MONTH] == 3

        private val LOG : Logger = LoggerFactory.getLogger(SurpriseController::class.java)

        private val MAILS : Array<String> = arrayOf("Da narasteš veliki, sve želje ti se ostvare, pa da se vidimo kad sve ovo prođe!<br>P.S. Hristos se rodi!",
                "Već si dobio lepe želje, koliko bi ti?", "Ok, dosta", "Vidim narastaš u QA testera",
        "Dobar pokušaj.", "Okej, važi, ipak ti je rođendan. Šta želiš za poklon?<br><ol><li><a href=\"http://webwatcher.ml/surprise/panda\">Pandu (kao pandemija hehe)</a></li>" +
                "<li><a href=\"http://webwatcher.ml/surprise/srecaizdravlje\">Sreće i zdravlja</a></li>" +
                "<li><a href=\"http://webwatcher.ml/surprise/ispiti\">Da položiš sve ispite</a></li>" +
                "<li><a href=\"http://webwatcher.ml/surprise/mrav\">Da budeš mali mrav, vredni mrav</a></li></ol>",
        "Nema više, to je to, stvarno sad.")
        private val MAIL_IS_HTML : Array<Boolean> = arrayOf(true, false, false, false, false, true, false)
        private var MAIL_COUNTER = 0
        private var ipfilter = true
    }

    @GetMapping("/test")
    fun serveSurprise(req: ServletWebRequest) : ResponseEntity<Any> {
        if(!isTurnedOn()) return ResponseEntity.notFound().build()
        val ip = req.request.remoteAddr
        LOG.info("Serving surprise to $ip")
        val sendMailTo = File("/home/luka/Documents/surprisemail").readText().trim()
        val index = min(MAIL_COUNTER, MAILS.size-1)
        return if(!ipfilter || ip.startsWith("89.216") || ip.startsWith("77.243") || ip.startsWith("37.19.1")) {
            Executor.sendMail("Srećan rođendan!", MAILS[index], MAIL_IS_HTML[index],
                    Email("surprise@webwatcher.ml"), Email(sendMailTo))
            LOG.info("Sent mail $MAIL_COUNTER to $sendMailTo")
            MAIL_COUNTER++
            ResponseEntity.ok(File("/home/luka/Documents/surprise.html").readText())
        } else {
            ResponseEntity.ok("Hello world!")
        }
    }

    @GetMapping("/surprise/reset")
    fun reset() : ResponseEntity<Any> {
        if(!isTurnedOn()) return ResponseEntity.notFound().build()
        MAIL_COUNTER = 0
        LOG.info("Reset surprise counter to 0")
        return ResponseEntity.ok().build()
    }

    @GetMapping("/surprise/status")
    fun status() : ResponseEntity<Any> {
        if(!isTurnedOn()) return ResponseEntity.notFound().build()
        LOG.info("Requested surprise status")
        return ResponseEntity.ok(MAIL_COUNTER)
    }

    @GetMapping("/surprise/image")
    fun surpriseImage() : ResponseEntity<Any> {
        if(!isTurnedOn()) return ResponseEntity.notFound().build()
        LOG.info("Requested surprise image")
        return ResponseEntity.ok(File("/home/luka/Documents/fd057076-9f20-4eaa-b6cb-520679251d24.JPG").readBytes())
    }

    @GetMapping("/surprise/srecaizdravlje")
    fun srecaIZdravlje() : ResponseEntity<Any> {
        if(!isTurnedOn()) return ResponseEntity.notFound().build()
        LOG.info("Requested sreca i zdravlje")
        return ResponseEntity.status(HttpStatus.PERMANENT_REDIRECT)
                .header("Location", "https://www.alo.rs/zenske-price/molitva-za-zdravlje-i-srecu-nasih-najmilijih-ove-reci-imaju-cudesnu-moc/295124/vest")
                .build()
    }

    @GetMapping("/surprise/panda")
    fun panda() : ResponseEntity<Any> {
        if(!isTurnedOn()) return ResponseEntity.notFound().build()
        LOG.info("Requested panda")
        return ResponseEntity.ok(File("/home/luka/Documents/panda.html").readText())
    }
    @GetMapping("/surprise/panda/image")
    fun pandaImage() : ResponseEntity<Any> {
        if(!isTurnedOn()) return ResponseEntity.notFound().build()
        LOG.info("Requested panda image")
        return ResponseEntity.ok(File("/home/luka/Documents/panda.webp").readBytes())
    }

    @GetMapping("/surprise/ispiti")
    fun ispiti() : ResponseEntity<Any> {
        if(!isTurnedOn()) return ResponseEntity.notFound().build()
        LOG.info("Requested ispiti")
        return ResponseEntity.status(HttpStatus.PERMANENT_REDIRECT)
                .header("Location", "https://www.youtube.com/watch?v=KhB42hQB9fQ&list=PLc6XnwM8b1YuEYW6nu3aU74pIWH-IdIJ5")
                .build();
    }

    @GetMapping("/surprise/mrav")
    fun mrav() : ResponseEntity<Any> {
        if(!isTurnedOn()) return ResponseEntity.notFound().build()
        LOG.info("Requested mrav")
        return ResponseEntity.status(HttpStatus.PERMANENT_REDIRECT)
                .header("Location", "https://www.facebook.com/groups/1416375691836223/")
                .build();
    }

    @GetMapping("/surprise/ipfilter")
    fun toggleIpFilter() : ResponseEntity<Any> {
        if(!isTurnedOn()) return ResponseEntity.notFound().build()
        LOG.info(if(ipfilter) "Turned off IP filter" else "Turned on IP Filter");
        ipfilter = !ipfilter
        return ResponseEntity.ok(ipfilter)
    }
}