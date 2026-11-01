package com.orthanc.orthanc_prototype.assignment.adapter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.orthanc.orthanc_prototype.assignment.domain.AssignmentRuleEvaluator;

@Configuration(proxyBeanMethods = false)
class AssignmentConfig {

	@Bean
	AssignmentRuleEvaluator assignmentRuleEvaluator() {
		return new AssignmentRuleEvaluator();
	}

}
