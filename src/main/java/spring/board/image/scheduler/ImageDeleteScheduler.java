package spring.board.image.scheduler;

import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import spring.board.image.domain.Image;
import spring.board.image.repository.ImageRepository;
import spring.board.image.service.ImageService;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class ImageDeleteScheduler {
    private final ImageRepository imageRepository;
    private final ImageService imageService;

    public ImageDeleteScheduler(ImageService imageService, ImageRepository imageRepository){
        this.imageRepository = imageRepository;
        this.imageService = imageService;
    }

    @Scheduled(fixedDelay = 600000) //ms
    @Transactional
    public void deleteUnusedImages(){
        LocalDateTime cutoff=LocalDateTime.now().minusHours(1);
        List<Image> images = imageRepository.findByPostIsNullAndUploadedAtBefore(cutoff);

        for (Image image : images){
            imageService.deleteByImageUrl(image.getUrl());
            imageRepository.delete(image);
        }
    }
}
