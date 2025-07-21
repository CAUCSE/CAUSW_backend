package net.causw.app.main.dto.util.dtoMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import net.causw.app.main.domain.model.entity.form.Form;
import net.causw.app.main.domain.model.entity.form.FormQuestion;
import net.causw.app.main.domain.model.entity.form.FormQuestionOption;
import net.causw.app.main.domain.model.entity.form.Reply;
import net.causw.app.main.domain.model.entity.form.ReplyQuestion;
import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.domain.model.entity.userCouncilFee.UserCouncilFee;
import net.causw.app.main.dto.form.response.FormResponseDto;
import net.causw.app.main.dto.form.response.FormResponseDto.FormResponseDtoBuilder;
import net.causw.app.main.dto.form.response.OptionResponseDto;
import net.causw.app.main.dto.form.response.OptionResponseDto.OptionResponseDtoBuilder;
import net.causw.app.main.dto.form.response.OptionSummaryResponseDto;
import net.causw.app.main.dto.form.response.OptionSummaryResponseDto.OptionSummaryResponseDtoBuilder;
import net.causw.app.main.dto.form.response.QuestionResponseDto;
import net.causw.app.main.dto.form.response.QuestionResponseDto.QuestionResponseDtoBuilder;
import net.causw.app.main.dto.form.response.QuestionSummaryResponseDto;
import net.causw.app.main.dto.form.response.QuestionSummaryResponseDto.QuestionSummaryResponseDtoBuilder;
import net.causw.app.main.dto.form.response.reply.ReplyQuestionResponseDto;
import net.causw.app.main.dto.form.response.reply.ReplyQuestionResponseDto.ReplyQuestionResponseDtoBuilder;
import net.causw.app.main.dto.form.response.reply.ReplyResponseDto;
import net.causw.app.main.dto.form.response.reply.ReplyResponseDto.ReplyResponseDtoBuilder;
import net.causw.app.main.dto.form.response.reply.ReplyUserResponseDto;
import net.causw.app.main.dto.form.response.reply.ReplyUserResponseDto.ReplyUserResponseDtoBuilder;
import net.causw.app.main.dto.form.response.reply.UserReplyResponseDto;
import net.causw.app.main.dto.form.response.reply.UserReplyResponseDto.UserReplyResponseDtoBuilder;
import net.causw.app.main.dto.form.response.reply.excel.ExcelReplyQuestionResponseDto;
import net.causw.app.main.dto.form.response.reply.excel.ExcelReplyQuestionResponseDto.ExcelReplyQuestionResponseDtoBuilder;
import net.causw.app.main.dto.form.response.reply.excel.ExcelReplyResponseDto;
import net.causw.app.main.dto.form.response.reply.excel.ExcelReplyResponseDto.ExcelReplyResponseDtoBuilder;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-07-21T16:18:46+0900",
    comments = "version: 1.4.2.Final, compiler: javac, environment: Java 17.0.13 (Homebrew)"
)
@Component
public class FormDtoMapperImpl implements FormDtoMapper {

    @Override
    public FormResponseDto toFormResponseDto(Form form, List<QuestionResponseDto> questionResponseDtoList) {
        if ( form == null && questionResponseDtoList == null ) {
            return null;
        }

        FormResponseDtoBuilder formResponseDto = FormResponseDto.builder();

        if ( form != null ) {
            formResponseDto.formId( form.getId() );
            formResponseDto.title( form.getTitle() );
            formResponseDto.isClosed( form.getIsClosed() );
            formResponseDto.isAllowedEnrolled( form.getIsAllowedEnrolled() );
            formResponseDto.enrolledRegisteredSemesterList( getRegisteredSemesterList( form ) );
            formResponseDto.isNeedCouncilFeePaid( form.getIsNeedCouncilFeePaid() );
            formResponseDto.isAllowedLeaveOfAbsence( form.getIsAllowedLeaveOfAbsence() );
            formResponseDto.leaveOfAbsenceRegisteredSemesterList( getLeaveOfAbsenceRegisteredSemesterList( form ) );
            formResponseDto.isAllowedGraduation( form.getIsAllowedGraduation() );
        }
        if ( questionResponseDtoList != null ) {
            List<QuestionResponseDto> list2 = questionResponseDtoList;
            if ( list2 != null ) {
                formResponseDto.questionResponseDtoList( new ArrayList<QuestionResponseDto>( list2 ) );
            }
        }

        return formResponseDto.build();
    }

    @Override
    public QuestionResponseDto toQuestionResponseDto(FormQuestion formQuestion, List<OptionResponseDto> optionResponseDtoList) {
        if ( formQuestion == null && optionResponseDtoList == null ) {
            return null;
        }

        QuestionResponseDtoBuilder questionResponseDto = QuestionResponseDto.builder();

        if ( formQuestion != null ) {
            questionResponseDto.questionId( formQuestion.getId() );
            questionResponseDto.questionType( formQuestion.getQuestionType() );
            questionResponseDto.questionNumber( formQuestion.getNumber() );
            questionResponseDto.questionText( formQuestion.getQuestionText() );
            questionResponseDto.isMultiple( formQuestion.getIsMultiple() );
        }
        if ( optionResponseDtoList != null ) {
            List<OptionResponseDto> list = optionResponseDtoList;
            if ( list != null ) {
                questionResponseDto.optionResponseDtoList( new ArrayList<OptionResponseDto>( list ) );
            }
        }

        return questionResponseDto.build();
    }

    @Override
    public OptionResponseDto toOptionResponseDto(FormQuestionOption formQuestionOption) {
        if ( formQuestionOption == null ) {
            return null;
        }

        OptionResponseDtoBuilder optionResponseDto = OptionResponseDto.builder();

        optionResponseDto.optionId( formQuestionOption.getId() );
        optionResponseDto.optionNumber( formQuestionOption.getNumber() );
        optionResponseDto.optionText( formQuestionOption.getOptionText() );

        return optionResponseDto.build();
    }

    @Override
    public ReplyQuestionResponseDto toReplyQuestionResponseDto(ReplyQuestion replyQuestion) {
        if ( replyQuestion == null ) {
            return null;
        }

        ReplyQuestionResponseDtoBuilder replyQuestionResponseDto = ReplyQuestionResponseDto.builder();

        replyQuestionResponseDto.questionId( replyQuestionFormQuestionId( replyQuestion ) );
        replyQuestionResponseDto.questionAnswer( replyQuestion.getQuestionAnswer() );
        replyQuestionResponseDto.selectedOptionList( getSelectedOptionList( replyQuestion ) );

        return replyQuestionResponseDto.build();
    }

    @Override
    public ReplyResponseDto toReplyResponseDto(ReplyUserResponseDto replyUserResponseDto, List<ReplyQuestionResponseDto> replyQuestionResponseDtoList, LocalDateTime createdAt) {
        if ( replyUserResponseDto == null && replyQuestionResponseDtoList == null && createdAt == null ) {
            return null;
        }

        ReplyResponseDtoBuilder replyResponseDto = ReplyResponseDto.builder();

        if ( replyUserResponseDto != null ) {
            replyResponseDto.replyUserResponseDto( replyUserResponseDto );
        }
        if ( replyQuestionResponseDtoList != null ) {
            List<ReplyQuestionResponseDto> list = replyQuestionResponseDtoList;
            if ( list != null ) {
                replyResponseDto.replyQuestionResponseDtoList( new ArrayList<ReplyQuestionResponseDto>( list ) );
            }
        }
        if ( createdAt != null ) {
            replyResponseDto.createdAt( createdAt );
        }

        return replyResponseDto.build();
    }

    @Override
    public UserReplyResponseDto toUserReplyResponseDto(Reply reply, List<QuestionResponseDto> questionResponseDtoList) {
        if ( reply == null && questionResponseDtoList == null ) {
            return null;
        }

        UserReplyResponseDtoBuilder userReplyResponseDto = UserReplyResponseDto.builder();

        if ( reply != null ) {
            userReplyResponseDto.replyQuestionResponseDtoList( replyQuestionListToReplyQuestionResponseDtoList( reply.getReplyQuestionList() ) );
        }
        if ( questionResponseDtoList != null ) {
            List<QuestionResponseDto> list = questionResponseDtoList;
            if ( list != null ) {
                userReplyResponseDto.questionResponseDtoList( new ArrayList<QuestionResponseDto>( list ) );
            }
        }

        return userReplyResponseDto.build();
    }

    @Override
    public OptionSummaryResponseDto toOptionSummaryResponseDto(FormQuestionOption formQuestionOption, Long selectedCount) {
        if ( formQuestionOption == null && selectedCount == null ) {
            return null;
        }

        OptionSummaryResponseDtoBuilder optionSummaryResponseDto = OptionSummaryResponseDto.builder();

        if ( formQuestionOption != null ) {
            optionSummaryResponseDto.optionId( formQuestionOption.getId() );
            optionSummaryResponseDto.optionNumber( formQuestionOption.getNumber() );
            optionSummaryResponseDto.optionText( formQuestionOption.getOptionText() );
        }
        if ( selectedCount != null ) {
            optionSummaryResponseDto.selectedCount( selectedCount );
        }

        return optionSummaryResponseDto.build();
    }

    @Override
    public QuestionSummaryResponseDto toQuestionSummaryResponseDto(FormQuestion formQuestion, List<String> questionAnswerList, List<OptionSummaryResponseDto> optionSummaryResponseDtoList, Long numOfReply, Boolean isMultiple) {
        if ( formQuestion == null && questionAnswerList == null && optionSummaryResponseDtoList == null && numOfReply == null && isMultiple == null ) {
            return null;
        }

        QuestionSummaryResponseDtoBuilder questionSummaryResponseDto = QuestionSummaryResponseDto.builder();

        if ( formQuestion != null ) {
            questionSummaryResponseDto.questionId( formQuestion.getId() );
            questionSummaryResponseDto.questionText( formQuestion.getQuestionText() );
            questionSummaryResponseDto.questionType( formQuestion.getQuestionType() );
        }
        if ( questionAnswerList != null ) {
            List<String> list = questionAnswerList;
            if ( list != null ) {
                questionSummaryResponseDto.questionAnswerList( new ArrayList<String>( list ) );
            }
        }
        if ( optionSummaryResponseDtoList != null ) {
            List<OptionSummaryResponseDto> list1 = optionSummaryResponseDtoList;
            if ( list1 != null ) {
                questionSummaryResponseDto.optionSummarieList( new ArrayList<OptionSummaryResponseDto>( list1 ) );
            }
        }
        if ( numOfReply != null ) {
            questionSummaryResponseDto.numOfReply( numOfReply );
        }
        if ( isMultiple != null ) {
            questionSummaryResponseDto.isMultiple( isMultiple );
        }

        return questionSummaryResponseDto.build();
    }

    @Override
    public ReplyUserResponseDto toReplyUserResponseDto(User user, UserCouncilFee userCouncilFee, Boolean isAppliedThisSemester, Integer restOfSemester) {
        if ( user == null && userCouncilFee == null && isAppliedThisSemester == null && restOfSemester == null ) {
            return null;
        }

        ReplyUserResponseDtoBuilder replyUserResponseDto = ReplyUserResponseDto.builder();

        if ( user != null ) {
            replyUserResponseDto.userId( user.getId() );
            replyUserResponseDto.email( user.getEmail() );
            replyUserResponseDto.name( user.getName() );
            replyUserResponseDto.nickName( user.getNickname() );
            replyUserResponseDto.admissionYear( user.getAdmissionYear() );
            replyUserResponseDto.studentId( user.getStudentId() );
            replyUserResponseDto.major( user.getMajor() );
            replyUserResponseDto.phoneNumber( user.getPhoneNumber() );
            replyUserResponseDto.academicStatus( user.getAcademicStatus() );
            replyUserResponseDto.currentCompletedSemester( user.getCurrentCompletedSemester() );
            replyUserResponseDto.graduationYear( user.getGraduationYear() );
            replyUserResponseDto.graduationType( user.getGraduationType() );
            replyUserResponseDto.createdAt( user.getCreatedAt() );
        }
        if ( userCouncilFee != null ) {
            replyUserResponseDto.paidAt( userCouncilFee.getPaidAt() );
            replyUserResponseDto.numOfPaidSemester( userCouncilFee.getNumOfPaidSemester() );
            replyUserResponseDto.isRefunded( userCouncilFee.getIsRefunded() );
        }
        if ( isAppliedThisSemester != null ) {
            replyUserResponseDto.isAppliedThisSemester( isAppliedThisSemester );
        }
        if ( restOfSemester != null ) {
            replyUserResponseDto.restOfSemester( restOfSemester );
        }

        return replyUserResponseDto.build();
    }

    @Override
    public ExcelReplyQuestionResponseDto toExcelReplyQuestionResponseDto(ReplyQuestion replyQuestion) {
        if ( replyQuestion == null ) {
            return null;
        }

        ExcelReplyQuestionResponseDtoBuilder excelReplyQuestionResponseDto = ExcelReplyQuestionResponseDto.builder();

        excelReplyQuestionResponseDto.questionId( replyQuestionFormQuestionId( replyQuestion ) );
        excelReplyQuestionResponseDto.questionAnswer( replyQuestion.getQuestionAnswer() );
        excelReplyQuestionResponseDto.selectedOptionTextList( getSelectedOptionTextList( replyQuestion ) );

        return excelReplyQuestionResponseDto.build();
    }

    @Override
    public ExcelReplyResponseDto toExcelReplyResponseDto(ReplyUserResponseDto replyUserResponseDto, List<ExcelReplyQuestionResponseDto> excelReplyQuestionResponseDtoList, LocalDateTime createdAt) {
        if ( replyUserResponseDto == null && excelReplyQuestionResponseDtoList == null && createdAt == null ) {
            return null;
        }

        ExcelReplyResponseDtoBuilder excelReplyResponseDto = ExcelReplyResponseDto.builder();

        if ( replyUserResponseDto != null ) {
            excelReplyResponseDto.replyUserResponseDto( replyUserResponseDto );
        }
        if ( excelReplyQuestionResponseDtoList != null ) {
            List<ExcelReplyQuestionResponseDto> list = excelReplyQuestionResponseDtoList;
            if ( list != null ) {
                excelReplyResponseDto.excelReplyQuestionResponseDtoList( new ArrayList<ExcelReplyQuestionResponseDto>( list ) );
            }
        }
        if ( createdAt != null ) {
            excelReplyResponseDto.createdAt( createdAt );
        }

        return excelReplyResponseDto.build();
    }

    private String replyQuestionFormQuestionId(ReplyQuestion replyQuestion) {
        if ( replyQuestion == null ) {
            return null;
        }
        FormQuestion formQuestion = replyQuestion.getFormQuestion();
        if ( formQuestion == null ) {
            return null;
        }
        String id = formQuestion.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    protected List<ReplyQuestionResponseDto> replyQuestionListToReplyQuestionResponseDtoList(List<ReplyQuestion> list) {
        if ( list == null ) {
            return null;
        }

        List<ReplyQuestionResponseDto> list1 = new ArrayList<ReplyQuestionResponseDto>( list.size() );
        for ( ReplyQuestion replyQuestion : list ) {
            list1.add( toReplyQuestionResponseDto( replyQuestion ) );
        }

        return list1;
    }
}
