package com.resolvex.config;
import org.springframework.http.*;import org.springframework.web.bind.MethodArgumentNotValidException;import org.springframework.web.bind.annotation.*;import org.springframework.web.server.ResponseStatusException;import java.util.*;
@RestControllerAdvice public class ApiExceptionHandler{
 @ExceptionHandler(MethodArgumentNotValidException.class) ResponseEntity<Map<String,Object>> validation(MethodArgumentNotValidException e){String msg=e.getBindingResult().getFieldErrors().stream().map(x->x.getField()+": "+x.getDefaultMessage()).findFirst().orElse("Invalid request");return ResponseEntity.badRequest().body(Map.of("message",msg));}
 @ExceptionHandler(ResponseStatusException.class) ResponseEntity<Map<String,Object>> status(ResponseStatusException e){return ResponseEntity.status(e.getStatusCode()).body(Map.of("message",e.getReason()==null?"Request failed":e.getReason()));}
 @ExceptionHandler(Exception.class) ResponseEntity<Map<String,Object>> other(Exception e){return ResponseEntity.status(500).body(Map.of("message","Unexpected server error"));}
}
