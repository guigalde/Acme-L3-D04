/*
 * EmployerApplicationShowService.java
 *
 * Copyright (C) 2012-2023 Rafael Corchuelo.
 *
 * In keeping with the traditional purpose of furthering education and research, it is
 * the policy of the copyright owner to permit non-commercial use and redistribution of
 * this software. It has been tested carefully, but it is not guaranteed for any particular
 * purposes. The copyright owner does not offer any warranties or representations, nor do
 * they accept any liabilities with respect to them.
 */

package acme.features.assistant;

import java.util.Collection;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import acme.entities.courses.Course;
import acme.entities.tutorial.Tutorial;
import acme.framework.components.jsp.SelectChoices;
import acme.framework.components.models.Tuple;
import acme.framework.services.AbstractService;
import acme.roles.Assistant;

@Service
public class AssistantTutorialShowService extends AbstractService<Assistant, Tutorial> {

	// Internal state ---------------------------------------------------------

	@Autowired
	protected AssistantTutorialRepository repository;

	// AbstractService interface ----------------------------------------------


	@Override
	public void check() {
		boolean status;

		status = super.getRequest().hasData("id", int.class);

		super.getResponse().setChecked(status);
	}

	@Override
	public void authorise() {
		boolean status;
		int masterId;
		Tutorial tutorial;
		Assistant assistant;

		masterId = super.getRequest().getData("id", int.class);
		tutorial = this.repository.findOneTutorialById(masterId);
		assistant = tutorial == null ? null : tutorial.getAssistant();
		status = super.getRequest().getPrincipal().hasRole(assistant) || tutorial != null && !tutorial.isDraftMode();

		super.getResponse().setAuthorised(status);
	}

	@Override
	public void load() {
		Tutorial object;
		int id;

		id = super.getRequest().getData("id", int.class);
		object = this.repository.findOneTutorialById(id);

		super.getBuffer().setData(object);
	}

	@Override
	public void unbind(final Tutorial object) {
		Tuple tuple;
		Collection<Course> courses;
		SelectChoices choices;
		final String assistantName = object.getAssistant().getUserAccount().getUsername();
		final String courseTitle = object.getCourse().getTitle();

		courses = this.repository.findAllCourse().stream().filter(x -> !x.isDraftMode()).collect(Collectors.toList());
		choices = SelectChoices.from(courses, "title", object.getCourse());

		tuple = super.unbind(object, "code", "title", "summary", "goals", "estimatedTime", "draftMode", "course");
		tuple.put("courseList", choices);
		tuple.put("selectedCourse", choices.getSelected().getKey());
		System.out.println(choices.getSelected());
		System.out.println(choices.getSelected().getKey());
		System.out.println();
		tuple.put("assistantName", assistantName);
		tuple.put("courseTitle", courseTitle);

		super.getResponse().setData(tuple);

	}

}
