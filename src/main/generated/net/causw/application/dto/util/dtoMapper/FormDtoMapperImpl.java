package net.causw.application.dto.util.dtoMapper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.Generated;
import net.causw.adapter.persistence.form.Form;
import net.causw.adapter.persistence.form.Option;
import net.causw.adapter.persistence.form.Question;
import net.causw.adapter.persistence.form.Reply;
import net.causw.adapter.persistence.user.User;
import net.causw.application.dto.form.FormResponseDto;
import net.causw.application.dto.form.FormResponseDto.FormResponseDtoBuilder;
import net.causw.application.dto.form.OptionResponseDto;
import net.causw.application.dto.form.OptionResponseDto.OptionResponseDtoBuilder;
import net.causw.application.dto.form.OptionSummaryResponseDto;
import net.causw.application.dto.form.OptionSummaryResponseDto.OptionSummaryResponseDtoBuilder;
import net.causw.application.dto.form.QuestionReplyResponseDto;
import net.causw.application.dto.form.QuestionReplyResponseDto.QuestionReplyResponseDtoBuilder;
import net.causw.application.dto.form.QuestionResponseDto;
import net.causw.application.dto.form.QuestionResponseDto.QuestionResponseDtoBuilder;
import net.causw.application.dto.form.QuestionSummaryResponseDto;
import net.causw.application.dto.form.QuestionSummaryResponseDto.QuestionSummaryResponseDtoBuilder;
import net.causw.application.dto.form.ReplyUserResponseDto;
import net.causw.application.dto.form.ReplyUserResponseDto.ReplyUserResponseDtoBuilder;
import net.causw.application.dto.user.UserResponseDto;
import net.causw.application.dto.user.UserResponseDto.UserResponseDtoBuilder;
import net.causw.domain.model.enums.Role;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-09-13T05:58:22+0900",
    comments = "version: 1.4.2.Final, compiler: javac, environment: Java 17.0.12 (Azul Systems, Inc.)"
)
@Component
public class FormDtoMapperImpl implements FormDtoMapper {

    @Override
    public FormResponseDto toFormResponseDto(Form form) {
        if ( form == null ) {
            return null;
        }

        FormResponseDtoBuilder formResponseDto = FormResponseDto.builder();

        formResponseDto.title( form.getTitle() );
        formResponseDto.writer( userToUserResponseDto( form.getWriter() ) );
        Set<Integer> set = form.getAllowedGrades();
        if ( set != null ) {
            formResponseDto.allowedGrades( new HashSet<Integer>( set ) );
        }
        formResponseDto.questions( questionListToQuestionResponseDtoList( form.getQuestions() ) );

        return formResponseDto.build();
    }

    @Override
    public QuestionReplyResponseDto toQuestionReplyResponseDto(Reply reply) {
        if ( reply == null ) {
            return null;
        }

        QuestionReplyResponseDtoBuilder questionReplyResponseDto = QuestionReplyResponseDto.builder();

        questionReplyResponseDto.questionId( replyQuestionId( reply ) );
        questionReplyResponseDto.questionAnswer( reply.getQuestionAnswer() );
        List<Integer> list = reply.getSelectedOptions();
        if ( list != null ) {
            questionReplyResponseDto.selectedOptions( new ArrayList<Integer>( list ) );
        }

        return questionReplyResponseDto.build();
    }

    @Override
    public ReplyUserResponseDto toReplyUserResponseDto(User user, List<QuestionReplyResponseDto> replies) {
        if ( user == null && replies == null ) {
            return null;
        }

        ReplyUserResponseDtoBuilder replyUserResponseDto = ReplyUserResponseDto.builder();

        if ( user != null ) {
            replyUserResponseDto.userId( user.getId() );
            replyUserResponseDto.userName( user.getName() );
        }
        if ( replies != null ) {
            List<QuestionReplyResponseDto> list = replies;
            if ( list != null ) {
                replyUserResponseDto.replies( new ArrayList<QuestionReplyResponseDto>( list ) );
            }
        }

        return replyUserResponseDto.build();
    }

    @Override
    public OptionSummaryResponseDto toOptionSummaryResponseDto(Option option, Long selectedCount) {
        if ( option == null && selectedCount == null ) {
            return null;
        }

        OptionSummaryResponseDtoBuilder optionSummaryResponseDto = OptionSummaryResponseDto.builder();

        if ( option != null ) {
            optionSummaryResponseDto.optionText( option.getOptionText() );
        }
        if ( selectedCount != null ) {
            optionSummaryResponseDto.selectedCount( selectedCount );
        }

        return optionSummaryResponseDto.build();
    }

    @Override
    public QuestionSummaryResponseDto toQuestionSummaryResponseDto(Question question, List<String> questionAnswers, List<OptionSummaryResponseDto> optionSummaries) {
        if ( question == null && questionAnswers == null && optionSummaries == null ) {
            return null;
        }

        QuestionSummaryResponseDtoBuilder questionSummaryResponseDto = QuestionSummaryResponseDto.builder();

        if ( question != null ) {
            questionSummaryResponseDto.questionId( question.getId() );
            questionSummaryResponseDto.questionText( question.getQuestionText() );
        }
        if ( questionAnswers != null ) {
            List<String> list = questionAnswers;
            if ( list != null ) {
                questionSummaryResponseDto.questionAnswers( new ArrayList<String>( list ) );
            }
        }
        if ( optionSummaries != null ) {
            List<OptionSummaryResponseDto> list1 = optionSummaries;
            if ( list1 != null ) {
                questionSummaryResponseDto.optionSummaries( new ArrayList<OptionSummaryResponseDto>( list1 ) );
            }
        }

        return questionSummaryResponseDto.build();
    }

    protected UserResponseDto userToUserResponseDto(User user) {
        if ( user == null ) {
            return null;
        }

        UserResponseDtoBuilder userResponseDto = UserResponseDto.builder();

        userResponseDto.id( user.getId() );
        userResponseDto.email( user.getEmail() );
        userResponseDto.name( user.getName() );
        userResponseDto.studentId( user.getStudentId() );
        userResponseDto.admissionYear( user.getAdmissionYear() );
        Set<Role> set = user.getRoles();
        if ( set != null ) {
            userResponseDto.roles( new HashSet<Role>( set ) );
        }
        userResponseDto.state( user.getState() );
        userResponseDto.nickname( user.getNickname() );
        userResponseDto.major( user.getMajor() );
        userResponseDto.academicStatus( user.getAcademicStatus() );
        userResponseDto.currentCompletedSemester( user.getCurrentCompletedSemester() );
        userResponseDto.graduationYear( user.getGraduationYear() );
        userResponseDto.graduationType( user.getGraduationType() );
        userResponseDto.phoneNumber( user.getPhoneNumber() );

        return userResponseDto.build();
    }

    protected OptionResponseDto optionToOptionResponseDto(Option option) {
        if ( option == null ) {
            return null;
        }

        OptionResponseDtoBuilder optionResponseDto = OptionResponseDto.builder();

        optionResponseDto.id( option.getId() );
        optionResponseDto.optionText( option.getOptionText() );
        optionResponseDto.isSelected( option.getIsSelected() );

        return optionResponseDto.build();
    }

    protected List<OptionResponseDto> optionListToOptionResponseDtoList(List<Option> list) {
        if ( list == null ) {
            return null;
        }

        List<OptionResponseDto> list1 = new ArrayList<OptionResponseDto>( list.size() );
        for ( Option option : list ) {
            list1.add( optionToOptionResponseDto( option ) );
        }

        return list1;
    }

    protected QuestionResponseDto questionToQuestionResponseDto(Question question) {
        if ( question == null ) {
            return null;
        }

        QuestionResponseDtoBuilder questionResponseDto = QuestionResponseDto.builder();

        questionResponseDto.id( question.getId() );
        questionResponseDto.questionText( question.getQuestionText() );
        questionResponseDto.isMultiple( question.getIsMultiple() );
        questionResponseDto.options( optionListToOptionResponseDtoList( question.getOptions() ) );

        return questionResponseDto.build();
    }

    protected List<QuestionResponseDto> questionListToQuestionResponseDtoList(List<Question> list) {
        if ( list == null ) {
            return null;
        }

        List<QuestionResponseDto> list1 = new ArrayList<QuestionResponseDto>( list.size() );
        for ( Question question : list ) {
            list1.add( questionToQuestionResponseDto( question ) );
        }

        return list1;
    }

    private String replyQuestionId(Reply reply) {
        if ( reply == null ) {
            return null;
        }
        Question question = reply.getQuestion();
        if ( question == null ) {
            return null;
        }
        String id = question.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
