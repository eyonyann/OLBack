package org.example.onlinelearning.mappers;

import org.example.onlinelearning.dtos.ReviewDTO;
import org.example.onlinelearning.models.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.springframework.stereotype.Service;

@Service
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ReviewMapper {
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "courseId", source = "course.id")
    ReviewDTO toReviewDTO(Review review);

    @Mapping(target = "course.id", source = "courseId")
    Review toReview(ReviewDTO reviewDTO);
}
