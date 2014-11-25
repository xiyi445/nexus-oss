/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.coreui

import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import javax.validation.Valid
import javax.validation.constraints.NotNull
import javax.validation.groups.Default

import org.sonatype.configuration.validation.InvalidConfigurationException
import org.sonatype.configuration.validation.ValidationMessage
import org.sonatype.configuration.validation.ValidationResponse
import org.sonatype.nexus.extdirect.DirectComponent
import org.sonatype.nexus.extdirect.DirectComponentSupport
import org.sonatype.nexus.formfields.Selectable
import org.sonatype.nexus.scheduling.NexusTaskScheduler
import org.sonatype.nexus.scheduling.TaskConfiguration
import org.sonatype.nexus.scheduling.TaskDescriptor
import org.sonatype.nexus.scheduling.TaskInfo
import org.sonatype.nexus.scheduling.TaskInfo.CurrentState
import org.sonatype.nexus.scheduling.TaskInfo.EndState
import org.sonatype.nexus.scheduling.TaskInfo.RunState
import org.sonatype.nexus.scheduling.TaskInfo.State
import org.sonatype.nexus.scheduling.schedule.Cron
import org.sonatype.nexus.scheduling.schedule.Daily
import org.sonatype.nexus.scheduling.schedule.Hourly
import org.sonatype.nexus.scheduling.schedule.Manual
import org.sonatype.nexus.scheduling.schedule.Monthly
import org.sonatype.nexus.scheduling.schedule.Now
import org.sonatype.nexus.scheduling.schedule.Once
import org.sonatype.nexus.scheduling.schedule.Schedule
import org.sonatype.nexus.scheduling.schedule.Weekly
import org.sonatype.nexus.validation.Create
import org.sonatype.nexus.validation.Update
import org.sonatype.nexus.validation.Validate

import com.softwarementors.extjs.djn.config.annotations.DirectAction
import com.softwarementors.extjs.djn.config.annotations.DirectMethod
import org.apache.shiro.authz.annotation.RequiresAuthentication
import org.apache.shiro.authz.annotation.RequiresPermissions
import org.hibernate.validator.constraints.NotEmpty

/**
 * Task {@link DirectComponent}.
 *
 * @since 3.0
 */
@Named
@Singleton
@DirectAction(action = 'coreui_Task')
class TaskComponent
    extends DirectComponentSupport
{

  @Inject
  NexusTaskScheduler nexusScheduler

  @Inject
  List<TaskDescriptor> descriptors

  /**
   * Retrieve a list of scheduled tasks.
   */
  @DirectMethod
  @RequiresPermissions('nexus:tasks:read')
  List<TaskXO> read() {
    return nexusScheduler.listsTasks().findAll { TaskInfo task ->
      return task.configuration.visible ? task : null
    }.collect { TaskInfo task ->
      TaskXO result = asTaskXO(task)
      return result
    }
  }

  /**
   * Retrieve available task types.
   * @return a list of task types
   */
  @DirectMethod
  @RequiresPermissions('nexus:tasktypes:read')
  List<TaskTypeXO> readTypes() {
    return descriptors.findAll { descriptor ->
      descriptor.exposed ? descriptor : null
    }.collect { descriptor ->
      def result = new TaskTypeXO(
          id: descriptor.id,
          name: descriptor.name,
          formFields: descriptor.formFields()?.collect { formField ->
            def formFieldXO = new FormFieldXO(
                id: formField.id,
                type: formField.type,
                label: formField.label,
                helpText: formField.helpText,
                required: formField.required,
                regexValidation: formField.regexValidation,
                initialValue: formField.initialValue
            )
            if (formField instanceof Selectable) {
              formFieldXO.storeApi = formField.storeApi
              formFieldXO.storeFilters = formField.storeFilters
              formFieldXO.idMapping = formField.idMapping
              formFieldXO.nameMapping = formField.nameMapping
            }
            return formFieldXO
          }
      )
      return result
    }
  }

  /**
   * Creates a task.
   * @param taskXO to be created
   * @return created task
   */
  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('nexus:tasks:create')
  @Validate(groups = [Create.class, Default.class])
  TaskXO create(final @NotNull(message = '[taskXO] may not be null') @Valid TaskXO taskXO) {
    Schedule schedule = asSchedule(taskXO)

    TaskConfiguration nexusTask = nexusScheduler.createTaskConfigurationInstance(taskXO.typeId)
    taskXO.properties.each { key, value ->
      nexusTask.getMap().put(key, value)
    }
    nexusTask.setAlertEmail(taskXO.alertEmail)
    // TODO: this should not be set by user, it's description instead
    nexusTask.setName(taskXO.name)
    nexusTask.setDescription(taskXO.description)
    nexusTask.setEnabled(taskXO.enabled)

    TaskInfo<?> task = nexusScheduler.scheduleTask(nexusTask, schedule)

    log.debug "Created task with type '${nexusTask.class}': ${nexusTask.name} (${nexusTask.id})"
    return asTaskXO(task)
  }

  /**
   * Updates a task.
   * @param taskXO to be updated
   * @return updated task
   */
  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('nexus:tasks:update')
  @Validate(groups = [Update.class, Default.class])
  TaskXO update(final @NotNull(message = '[taskXO] may not be null') @Valid TaskXO taskXO) {
    TaskInfo task = nexusScheduler.getTaskById(taskXO.id);
    validateState(task)
    task.configuration.enabled = taskXO.enabled
    task.configuration.name = taskXO.name
    task.configuration.description = taskXO.description
    task.configuration.map.putAll(taskXO.properties)
    task.configuration.setAlertEmail(taskXO.alertEmail)
    task.configuration.setName(taskXO.name)

    nexusScheduler.scheduleTask(task.configuration, task.schedule)

    return asTaskXO(task)
  }

  /**
   * Updates a task schedule.
   * @param taskXO to be updated
   * @return updated task
   */
  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('nexus:tasks:update')
  @Validate(groups = [Schedule.class, Default.class])
  TaskXO updateSchedule(final @NotNull(message = '[taskXO] may not be null') @Valid TaskXO taskXO) {
    TaskInfo task = nexusScheduler.getTaskById(taskXO.id);
    validateState(task)
    Schedule schedule = asSchedule(taskXO)

    nexusScheduler.scheduleTask(task.configuration, schedule)

    return asTaskXO(task)
  }

  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('nexus:tasks:delete')
  @Validate
  void remove(final @NotEmpty(message = '[id] may not be empty') String id) {
    nexusScheduler.getTaskById(id)?.remove()
  }

  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('nexus:tasksrun:get')
  @Validate
  void run(final @NotEmpty(message = '[id] may not be empty') String id) {
    nexusScheduler.getTaskById(id)?.runNow()
  }

  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('nexus:tasksrun:delete')
  @Validate
  void stop(final @NotEmpty(message = '[id] may not be empty') String id) {
    nexusScheduler.getTaskById(id)?.currentState?.future?.cancel(true)
  }

  static String getStatusDescription(final CurrentState<?> currentState) {
    switch (currentState.state) {
      case State.WAITING:
        return 'Waiting'
      case State.RUNNING:
        switch (currentState.runState) {
          case RunState.RUNNING:
            return "Running";
          case RunState.BLOCKED:
            return "Blocked";
          case RunState.CANCELED:
            return "Cancelling";
          default:
            return 'Unknown running'
        }
      default:
        return 'Unknown'
    }
  }

  static String getSchedule(final Schedule schedule) {
    if (schedule instanceof Manual) {
      return 'manual'
    }
    else if (schedule instanceof Now) {
      return 'internal'
    }
    else if (schedule instanceof Once) {
      return 'once'
    }
    else if (schedule instanceof Hourly) {
      return 'hourly'
    }
    else if (schedule instanceof Daily) {
      return 'daily'
    }
    else if (schedule instanceof Weekly) {
      return 'weekly'
    }
    else if (schedule instanceof Monthly) {
      return 'monthly'
    }
    else if (schedule instanceof Cron) {
      return 'advanced'
    }
    else {
      return schedule.getClass().getName()
    }
  }

  static Date getNextRun(final TaskInfo<?> task) {
    return task.currentState.nextRun;
  }

  static String getLastRunResult(final TaskInfo<?> task) {
    String lastRunResult = null

    if (task.lastRunState != null) {
      lastRunResult = null;
      switch (task.lastRunState.endState) {
        case EndState.OK:
          lastRunResult = "Ok";
          break;
        case EndState.CANCELED:
          lastRunResult = "Canceled";
          break;
        case EndState.FAILED:
          lastRunResult = "Error";
          break;
        default:
          lastRunResult = task.lastRunState.endState.name();
      }
      if (task.lastRunState.runDuration != 0) {
        long milliseconds = task.lastRunState.runDuration

        int hours = (int) ((milliseconds / 1000) / 3600)
        int minutes = (int) ((milliseconds / 1000) / 60 - hours * 60)
        int seconds = (int) (((long) (milliseconds / 1000)) % 60)

        lastRunResult += " ["
        if (hours != 0) {
          lastRunResult += hours
          lastRunResult += "h"
        }
        if (minutes != 0 || hours != 0) {
          lastRunResult += minutes
          lastRunResult += "m"
        }
        lastRunResult += seconds
        lastRunResult += "s"
        lastRunResult += "]"
      }
    }
    return lastRunResult
  }

  TaskXO asTaskXO(final TaskInfo<?> task) {
    def result = new TaskXO(
        id: task.id,
        enabled: task.configuration.enabled,
        name: task.name,
        typeId: task.configuration.type,
        typeName: (descriptors.find { it.id == task.configuration.type })?.name,
        status: task.currentState.state,
        statusDescription: task.configuration.enabled ? getStatusDescription(task.currentState) : 'Disabled',
        schedule: getSchedule(task.schedule),
        lastRun: task.lastRunState?.runStarted,
        lastRunResult: getLastRunResult(task),
        nextRun: getNextRun(task),
        runnable: task.currentState.state in [State.WAITING],
        stoppable: task.currentState.state in [State.RUNNING],
        alertEmail: task.configuration.alertEmail,
        properties: task.configuration.map
    )
    def schedule = task.schedule
    if (schedule instanceof Hourly) {
      result.startDate = schedule.startAt
    }
    if (schedule instanceof Daily) {
      result.startDate = schedule.startAt
    }
    if (schedule instanceof Weekly) {
      result.startDate = schedule.startAt
      result.recurringDays = schedule.daysToRun
    }
    if (schedule instanceof Monthly) {
      result.startDate = schedule.startAt
      result.recurringDays = schedule.daysToRun
    }
    if (schedule instanceof Cron) {
      result.startDate = schedule.startAt
      result.cronExpression = schedule.cronExpression
    }
    result
  }

  static Schedule asSchedule(final TaskXO taskXO) {
    if (taskXO.schedule == 'advanced') {
      try {
        return new Cron(new Date(), taskXO.cronExpression)
      }
      catch (Exception e) {
        def response = new ValidationResponse()
        response.addValidationError(new ValidationMessage('cronExpression', e.getMessage()))
        throw new InvalidConfigurationException(response)
      }
    }
    if (taskXO.schedule != 'manual') {
      if (!taskXO.startDate) {
        def response = new ValidationResponse()
        response.addValidationError(new ValidationMessage('startDate', 'May not be null'))
        throw new InvalidConfigurationException(response)
      }
      def date = Calendar.instance
      date.setTimeInMillis(taskXO.startDate.time)
      date.set(Calendar.SECOND, 0)
      date.set(Calendar.MILLISECOND, 0)
      switch (taskXO.schedule) {
        case 'once':
          def currentDate = Calendar.instance
          if (currentDate.after(date)) {
            def response = new ValidationResponse()
            if (currentDate.get(Calendar.YEAR) == date.get(Calendar.YEAR)
                && currentDate.get(Calendar.MONTH) == date.get(Calendar.MONTH)
                && currentDate.get(Calendar.DAY_OF_YEAR) == date.get(Calendar.DAY_OF_YEAR)) {
              response.addValidationError(new ValidationMessage('startTime', 'Time is in the past'))
            }
            else {
              response.addValidationError(new ValidationMessage('startDate', 'Date is in the past'))
            }
            throw new InvalidConfigurationException(response)
          }
          return new Once(date.time)
        case 'hourly':
          return new Hourly(date.time)
        case 'daily':
          return new Daily(date.time)
        case 'weekly':
          return new Weekly(date.time, taskXO.recurringDays as Set<Integer>)
        case 'monthly':
          return new Monthly(date.time, taskXO.recurringDays as Set<Integer>)
      }
    }
    return new Manual()
  }

  private static void validateState(final TaskInfo<?> task) {
    State state = task.currentState.state;
    if (State.RUNNING == state) {
      throw new Exception('Task can\'t be edited while it is being executed or it is in line to be executed');
    }
  }

}
