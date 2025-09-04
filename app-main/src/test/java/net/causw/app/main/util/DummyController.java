package net.causw.app.main.util;

import static org.springframework.web.bind.annotation.RequestMethod.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DummyController {
	@RequestMapping(value = "/**", method = {GET, POST, PUT, DELETE, PATCH, OPTIONS, HEAD, TRACE})
	public ResponseEntity<Void> fallback() {
		return ResponseEntity.notFound().build();
	}
}
