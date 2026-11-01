package com.orthanc.orthanc_prototype.assignment.adapter.in.web;

import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.orthanc.orthanc_prototype.assignment.domain.AssignmentRuleNotFoundException;
import com.orthanc.orthanc_prototype.assignment.domain.DuplicateRulePriorityException;
import com.orthanc.orthanc_prototype.assignment.domain.InvalidAssignmentRuleException;

@RestControllerAdvice(basePackageClasses = AssignmentExceptionHandler.class)
class AssignmentExceptionHandler extends ResponseEntityExceptionHandler {

	@ExceptionHandler(InvalidAssignmentRuleException.class)
	ProblemDetail handleInvalidRule(InvalidAssignmentRuleException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
	}

	@ExceptionHandler(AssignmentRuleNotFoundException.class)
	ProblemDetail handleNotFound(AssignmentRuleNotFoundException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
	}

	@ExceptionHandler(DuplicateRulePriorityException.class)
	ProblemDetail handleDuplicatePriority(DuplicateRulePriorityException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
	}

	@ExceptionHandler({ OptimisticLockingFailureException.class, DataIntegrityViolationException.class })
	ProblemDetail handleConcurrentModification(RuntimeException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
				"The assignment rule was modified concurrently, please retry");
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		String detail = ex.getBindingResult().getFieldErrors().stream()
			.map(error -> error.getField() + ": " + error.getDefaultMessage())
			.sorted()
			.collect(Collectors.joining("; "));
		if (!detail.isEmpty()) {
			ex.getBody().setDetail(detail);
		}
		return super.handleMethodArgumentNotValid(ex, headers, status, request);
	}

}
