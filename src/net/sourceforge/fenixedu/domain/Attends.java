/*
 * Attends.java
 *
 * Created on 20 de Outubro de 2002, 14:42
 */

package net.sourceforge.fenixedu.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import net.sourceforge.fenixedu.domain.exceptions.DomainException;
import net.sourceforge.fenixedu.domain.student.WeeklyWorkLoad;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.YearMonthDay;

/**
 * 
 * @author tfc130
 */
public class Attends extends Attends_Base {

	public static final Comparator<Attends> ATTENDS_COMPARATOR = new Comparator<Attends>() {
		public int compare(final Attends attends1, final Attends attends2) {
			final ExecutionCourse executionCourse1 = attends1.getDisciplinaExecucao();
			final ExecutionCourse executionCourse2 = attends2.getDisciplinaExecucao();
			if (executionCourse1 == executionCourse2) {
				final Student student1 = attends1.getAluno();
				final Student student2 = attends2.getAluno();
				return student1.getNumber().compareTo(student2.getNumber());
			} else {
				final ExecutionPeriod executionPeriod1 = executionCourse1.getExecutionPeriod();
				final ExecutionPeriod executionPeriod2 = executionCourse2.getExecutionPeriod();
				if (executionPeriod1 == executionPeriod2) {
					return executionCourse1.getNome().compareTo(executionCourse2.getNome());
				} else {
					return executionPeriod1.compareTo(executionPeriod2);
				}
			}
		}
	};

    public Attends() {
    	super();
    	setRootDomainObject(RootDomainObject.getInstance());
    }
	
	public Attends (Student student, ExecutionCourse executionCourse) {
		this();
		setAluno(student);
		setDisciplinaExecucao(executionCourse);
	}
	
	public void delete() throws DomainException {
		
		if (!hasAnyShiftEnrolments() && !hasAnyStudentGroups() && !hasAnyAssociatedMarks()) {
			removeAluno();
			removeDisciplinaExecucao();
			removeEnrolment();
			super.deleteDomainObject();
		}
		else
			throw new DomainException("error.attends.cant.delete");
	}
	
	private boolean hasAnyShiftEnrolments() {
	    for (Shift shift : this.getDisciplinaExecucao().getAssociatedShifts()) {
            if (shift.getStudents().contains(this.getAluno())) {
                return true;
            }
        }
        return false;
    }

    public FinalMark getFinalMark() {
		for (Mark mark : getAssociatedMarks()) {
			if(mark instanceof FinalMark) {
				return (FinalMark) mark;
			}
		}
		return null;
	}

	public Mark getMarkByEvaluation(Evaluation evaluation) {
		for (Mark mark : getAssociatedMarks()) {
			if(mark.getEvaluation().equals(evaluation)) {
				return mark;
			}
		}
		return null;
	}

    public List<Mark> getAssociatedMarksOrderedByEvaluationDate() {
        final List<Evaluation> orderedEvaluations = getDisciplinaExecucao().getOrderedAssociatedEvaluations();
        final List<Mark> orderedMarks = new ArrayList<Mark>(orderedEvaluations.size());
        for (int i = 0; i < orderedEvaluations.size(); i++) {
            orderedMarks.add(null);
        }
        for (final Mark mark : getAssociatedMarks()) {
            final Evaluation evaluation = mark.getEvaluation();
            orderedMarks.set(orderedEvaluations.indexOf(evaluation), mark);
        }
        return orderedMarks;
    }

    public WeeklyWorkLoad createWeeklyWorkLoad(final Integer contact, final Integer autonomousStudy, final Integer other) {
        if (getEnrolment() == null) {
            throw new DomainException("weekly.work.load.creation.requires.enrolment");
        }

        final int currentWeekOffset = calculateCurrentWeekOffset();
        if (currentWeekOffset < 1 || new YearMonthDay(getEndOfExamsPeriod()).plusDays(7).isBefore(new YearMonthDay())) {
            throw new DomainException("outside.weekly.work.load.response.period");
        }

        final int previousWeekOffset = currentWeekOffset - 1;

        final WeeklyWorkLoad lastExistentWeeklyWorkLoad = getWeeklyWorkLoads().isEmpty() ?
                null : Collections.max(getWeeklyWorkLoads());
        if (lastExistentWeeklyWorkLoad != null && lastExistentWeeklyWorkLoad.getWeekOffset().intValue() == previousWeekOffset) {
            throw new DomainException("weekly.work.load.for.previous.week.already.exists");
        }

        return new WeeklyWorkLoad(this, Integer.valueOf(previousWeekOffset), contact, autonomousStudy, other);
    }

    public Interval getWeeklyWorkLoadInterval() {
        final DateTime beginningOfSemester = new DateTime(getBegginingOfLessonPeriod());
        final DateTime firstMonday = beginningOfSemester.withField(DateTimeFieldType.dayOfWeek(), 1);
        final DateTime endOfSemester = new DateTime(getEndOfExamsPeriod());
        final DateTime nextLastMonday = endOfSemester.withField(DateTimeFieldType.dayOfWeek(), 1).plusWeeks(1);
        return new Interval(firstMonday, nextLastMonday);
    }

    public WeeklyWorkLoad getWeeklyWorkLoadOfPreviousWeek() {
        final int currentWeekOffset = calculateCurrentWeekOffset();
        if (currentWeekOffset < 1 || new YearMonthDay(getEndOfExamsPeriod()).plusDays(7).isBefore(new YearMonthDay())) {
            throw new DomainException("outside.weekly.work.load.response.period");
        }
        final int previousWeekOffset = currentWeekOffset - 1;
        for (final WeeklyWorkLoad weeklyWorkLoad : getWeeklyWorkLoads()) {
        	if (weeklyWorkLoad.getWeekOffset().intValue() == previousWeekOffset) {
        		return weeklyWorkLoad;
        	}
        }
        return null;
    }

    public Interval getCurrentWeek() {
        final DateMidnight beginningOfSemester = new DateMidnight(getBegginingOfLessonPeriod());
        final DateMidnight firstMonday = beginningOfSemester.withField(DateTimeFieldType.dayOfWeek(), 1);
        final int currentWeek = calculateCurrentWeekOffset();
        final DateMidnight start = firstMonday.plusWeeks(currentWeek);
        return new Interval(start, start.plusWeeks(1));
    }

    public Interval getPreviousWeek() {
        final DateMidnight thisMonday = new DateMidnight().withField(DateTimeFieldType.dayOfWeek(), 1);
        final DateMidnight previousMonday = thisMonday.minusWeeks(1);
        return new Interval(previousMonday, thisMonday);
    }

    public Interval getResponseWeek() {
        final DateMidnight beginningOfSemester = new DateMidnight(getBegginingOfLessonPeriod());
        final DateMidnight firstMonday = beginningOfSemester.withField(DateTimeFieldType.dayOfWeek(), 1);
        final DateMidnight secondMonday = firstMonday.plusWeeks(1);

        final DateMidnight endOfSemester = new DateMidnight(getEndOfExamsPeriod());
        final DateMidnight lastMonday = endOfSemester.withField(DateTimeFieldType.dayOfWeek(), 1);
        final DateMidnight endOfResponsePeriod = lastMonday.plusWeeks(2);

        return (secondMonday.isEqualNow() || secondMonday.isBeforeNow()) && endOfResponsePeriod.isAfterNow()
        		? getPreviousWeek() : null;
    }

    public int getCalculatePreviousWeek() {
    	final int previousWeekOffset = calculateCurrentWeekOffset() - 1;
    	return Integer.valueOf(previousWeekOffset + 1);
    }

    public int calculateCurrentWeekOffset() {
        final DateMidnight beginningOfLessonPeriod = new DateMidnight(getBegginingOfLessonPeriod());
        final DateMidnight firstMonday = beginningOfLessonPeriod.withField(DateTimeFieldType.dayOfWeek(), 1);
        final DateMidnight thisMonday = new DateMidnight().withField(DateTimeFieldType.dayOfWeek(), 1);
        final Period period = new Period(firstMonday, thisMonday);
        return (period.getYears() * 12 + period.getMonths()) * 4 + period.getWeeks();
    }

    public Set<WeeklyWorkLoad> getSortedWeeklyWorkLoads() {
    	return new TreeSet<WeeklyWorkLoad>(getWeeklyWorkLoads());
    }

    public int getWeeklyWorkLoadContact() {
    	int result = 0;
    	for (final WeeklyWorkLoad weeklyWorkLoad : getWeeklyWorkLoads()) {
    		final int contact = weeklyWorkLoad.getContact() != null ? weeklyWorkLoad.getContact() : 0;
    		result += contact;
    	}
    	return result;
    }

    public int getWeeklyWorkLoadAutonomousStudy() {
    	int result = 0;
    	for (final WeeklyWorkLoad weeklyWorkLoad : getWeeklyWorkLoads()) {
    		final int contact = weeklyWorkLoad.getAutonomousStudy() != null ? weeklyWorkLoad.getAutonomousStudy() : 0;
    		result += contact;
    	}
    	return result;    	
    }

    public int getWeeklyWorkLoadOther() {
    	int result = 0;
    	for (final WeeklyWorkLoad weeklyWorkLoad : getWeeklyWorkLoads()) {
    		final int contact = weeklyWorkLoad.getOther() != null ? weeklyWorkLoad.getOther() : 0;
    		result += contact;
    	}
    	return result;
    }

    public int getWeeklyWorkLoadTotal() {
    	int result = 0;
    	for (final WeeklyWorkLoad weeklyWorkLoad : getWeeklyWorkLoads()) {
    		final int contact = weeklyWorkLoad.getTotal();
    		result += contact;
    	}
    	return result;
    }

    public Date getBegginingOfLessonPeriod() {
        final ExecutionPeriod executionPeriod = getDisciplinaExecucao().getExecutionPeriod();
        final StudentCurricularPlan studentCurricularPlan = getEnrolment().getStudentCurricularPlan();
        final ExecutionDegree executionDegree = studentCurricularPlan.getDegreeCurricularPlan().getExecutionDegreeByYear(executionPeriod.getExecutionYear());
        if (executionPeriod.getSemester().intValue() == 1) {
            return executionDegree.getPeriodLessonsFirstSemester().getStart();
        } else if (executionPeriod.getSemester().intValue() == 2) {
            return executionDegree.getPeriodLessonsSecondSemester().getStart();
        } else {
            throw new DomainException("unsupported.execution.period.semester");
        }
    }

    public Date getEndOfExamsPeriod() {
        final ExecutionPeriod executionPeriod = getDisciplinaExecucao().getExecutionPeriod();
        final StudentCurricularPlan studentCurricularPlan = getEnrolment().getStudentCurricularPlan();
        final ExecutionDegree executionDegree = studentCurricularPlan.getDegreeCurricularPlan().getExecutionDegreeByYear(executionPeriod.getExecutionYear());
        if (executionPeriod.getSemester().intValue() == 1) {
            return executionDegree.getPeriodExamsFirstSemester().getEnd();
        } else if (executionPeriod.getSemester().intValue() == 2) {
            return executionDegree.getPeriodExamsSecondSemester().getEnd();
        } else {
            throw new DomainException("unsupported.execution.period.semester");
        }
    }
}
