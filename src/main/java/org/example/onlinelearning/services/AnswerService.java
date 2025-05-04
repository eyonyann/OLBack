package org.example.onlinelearning.services;

import lombok.RequiredArgsConstructor;
import org.example.onlinelearning.dtos.AnswerDTO;
import org.example.onlinelearning.exceptions.NotFoundException;
import org.example.onlinelearning.mappers.AnswerMapper;
import org.example.onlinelearning.models.Answer;
import org.example.onlinelearning.models.Assignment;
import org.example.onlinelearning.repositories.AnswerRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnswerService {
    private final AnswerRepository answerRepository;
    private final AnswerMapper answerMapper;
    private final AssignmentService assignmentService;

    public AnswerDTO getAnswerById(Long id) {
        Answer answer = answerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Answer not found with id: " + id));
        return answerMapper.toAnswerDTO(answer);
    }

    public AnswerDTO createAnswer(Long assignmentId, AnswerDTO answerDTO) {
        Assignment assignment = assignmentService.getAssignmentEntityById(assignmentId);

        Answer answer = answerMapper.toAnswer(answerDTO);
        answer.setAssignment(assignment);
        answer.setTime(LocalDateTime.now());

        Answer savedAnswer = answerRepository.save(answer);
        return answerMapper.toAnswerDTO(savedAnswer);
    }

    public AnswerDTO updateAnswer(Long id, AnswerDTO answerDTO) {
        Answer existingAnswer = answerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Answer not found with id: " + id));

        existingAnswer.setContent(answerDTO.getContent());
        Answer updatedAnswer = answerRepository.save(existingAnswer);
        return answerMapper.toAnswerDTO(updatedAnswer);
    }

    public void deleteAnswer(Long id) {
        if (!answerRepository.existsById(id)) {
            throw new NotFoundException("Answer not found with id: " + id);
        }
        answerRepository.deleteById(id);
    }

    public List<AnswerDTO> getAllAnswersByAssignmentId(Long assignmentId) {
        List<Answer> answers = answerRepository.findByAssignmentId(assignmentId);
        if (answers.isEmpty()) {
            throw new NotFoundException("No answers found for assignment id: " + assignmentId);
        }
        return answers.stream()
                .map(answerMapper::toAnswerDTO)
                .toList();
    }

    public AnswerDTO getUserAnswerForAssignment(Long assignmentId, Long userId) {
        return answerRepository.findByAssignmentIdAndUserId(assignmentId, userId)
                .map(answerMapper::toAnswerDTO)
                .orElseThrow(() -> new NotFoundException("Answer not found for assignment and user"));
    }
}