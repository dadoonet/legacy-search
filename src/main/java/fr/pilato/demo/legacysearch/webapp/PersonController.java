package fr.pilato.demo.legacysearch.webapp;

import fr.pilato.demo.legacysearch.domain.Person;
import fr.pilato.demo.legacysearch.service.PersonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class PersonController {
    private final Logger logger = LoggerFactory.getLogger(PersonController.class);

    private final PersonService personService;

    @Autowired
    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    @GetMapping("/_byid/{id}")
    @ResponseBody
    public Person get(@RequestPart Integer id) {
        return personService.get(id);
    }

    @GetMapping("/greeting")
    public String greeting(@RequestParam(name="name", required=false, defaultValue="World") String name, Model model) {
        model.addAttribute("name", name);
        return "greeting";
    }

    /**
     * Create or update an entity
     */
    @PutMapping("/{id}")
    @ResponseBody
    public Person upsert(@RequestPart Integer id, @RequestBody Person person) {
        logger.debug("upsert({}, {})", id, person);
        Person upsert = personService.upsert(id, person);
        logger.debug("created/updated {}: {}", id, upsert);
        return upsert;
    }

    @DeleteMapping("/{id}")
    public void delete(@RequestPart Integer id) {
        logger.debug("Person: {}", id);
        personService.delete(id);
    }

    @GetMapping("/_search")
    @ResponseBody
    public String search(@RequestParam(required = false) String q, @RequestParam(required = false) String f_country,
                         @RequestParam(required = false) String f_date, @RequestParam(required = false, defaultValue = "0") Integer from,
                         @RequestParam(required = false, defaultValue = "10") Integer size) throws IOException {
        return personService.search(q, f_country, f_date, from, size);
    }

    @GetMapping("/_advanced_search")
    @ResponseBody
    public String advancedSearch(@RequestParam(required = false) String name, @RequestParam(required = false) String country,
                                 @RequestParam(required = false) String city,
                                 @RequestParam(required = false, defaultValue = "0") Integer from,
                                 @RequestParam(required = false, defaultValue = "10") Integer size) throws IOException {
        return personService.advancedSearch(name, country, city, from, size);
    }

    @GetMapping("/_init")
    @ResponseBody
    public InitResult init(@RequestParam(required = false, defaultValue = "1000") Integer size) throws IOException {
        return personService.init(size);
    }

    @GetMapping("/_init_status")
    @ResponseBody
    public InitResult initStatus() {
        return personService.getInitCurrentAchievement();
    }
}
