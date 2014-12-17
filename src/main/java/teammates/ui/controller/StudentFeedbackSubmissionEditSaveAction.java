package teammates.ui.controller;

import teammates.common.datatransfer.FeedbackSessionAttributes;
import teammates.common.datatransfer.FeedbackSessionQuestionsBundle;
import teammates.common.datatransfer.StudentAttributes;
import teammates.common.exception.EntityDoesNotExistException;
import teammates.common.exception.InvalidParametersException;
import teammates.common.util.Const;
import teammates.logic.api.GateKeeper;

public class StudentFeedbackSubmissionEditSaveAction extends FeedbackSubmissionEditSaveAction {
    
    @Override
    protected void verifyAccesibleForSpecificUser() {
        new GateKeeper().verifyAccessible(
                getStudent(),
                logic.getFeedbackSession(feedbackSessionName, courseId));
    }

    @Override
    protected void appendRespondant() {
        try {
            logic.addStudentRespondant(getUserEmailForCourse(), feedbackSessionName, courseId);
        } catch (InvalidParametersException | EntityDoesNotExistException e) {
            log.severe("Fail to append student respondant");
        }
    }

    @Override
    protected void removeRespondant() {
        try {
            logic.deleteStudentRespondant(getUserEmailForCourse(), feedbackSessionName, courseId);
        } catch (InvalidParametersException | EntityDoesNotExistException e) {
            log.severe("Fail to remove student respondant");
        }
    }

    @Override
    protected String getUserEmailForCourse() {
        return getStudent().email;
    }
    
    @Override 
    protected String getUserSectionForCourse() {
        return getStudent().section;
    }

    @Override
    protected FeedbackSessionQuestionsBundle getDataBundle(String userEmailForCourse)
            throws EntityDoesNotExistException {
        return logic.getFeedbackSessionQuestionsBundleForStudent(
                feedbackSessionName, courseId, userEmailForCourse);
    }

    @Override
    protected void setStatusToAdmin() {
        statusToAdmin = "Show student feedback edit result page<br>" +
                "Session Name: " + feedbackSessionName + "<br>" +
                "Course ID: " + courseId;
    }

    @Override
    protected boolean isSessionOpenForSpecificUser(FeedbackSessionAttributes session) {
        return session.isOpened() || session.isInGracePeriod();
    }

    @Override
    protected RedirectResult createSpecificRedirectResult() {
        RedirectResult result = null;
        
        if(isRegisteredStudent()){
            if(isError){
                // Return to student feedback submission page if there is an error
                result =  createRedirectResult(Const.ActionURIs.STUDENT_FEEDBACK_SUBMISSION_EDIT_PAGE);
                
                // Provide course id and session name for the redirected page
                result.responseParams.put(Const.ParamsNames.COURSE_ID, student.course);
                result.responseParams.put(Const.ParamsNames.FEEDBACK_SESSION_NAME, 
                            getRequestParamValue(Const.ParamsNames.FEEDBACK_SESSION_NAME));
            }else{
                // Return to student home page if there is no error
                result =  createRedirectResult(Const.ActionURIs.STUDENT_HOME_PAGE);
            }
        }else{
            // Always remain at student feedback submission page
            // Link given to unregistered student already contains course id & session name
            result = createRedirectResult(Const.ActionURIs.STUDENT_FEEDBACK_SUBMISSION_EDIT_PAGE); 
        }
        return result;
    }

    protected StudentAttributes getStudent() {
        if (student == null) {
            student = logic.getStudentForGoogleId(courseId, account.googleId);
        }
        
        return student;
    }
    
    protected boolean isRegisteredStudent(){
        // a registered student must have an associated google Id
        if(student != null){
            return student.googleId != null  && !student.googleId.isEmpty();
        }else{
            return false;   
        }
    }
}