package com.rycus.Rycus_backend.user;

import com.rycus.Rycus_backend.post.Post;
import com.rycus.Rycus_backend.post.PostImage;
import com.rycus.Rycus_backend.post.UserPhotoDto;
import com.rycus.Rycus_backend.repository.PostImageRepository;
import com.rycus.Rycus_backend.repository.PostRepository;
import com.rycus.Rycus_backend.repository.UserRepository;
import com.rycus.Rycus_backend.user.dto.SafeUserDto;
import com.rycus.Rycus_backend.user.dto.UserMiniDto;
import com.rycus.Rycus_backend.user.dto.UserSearchDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/users")
@CrossOrigin
public class UserController {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;
    private final com.rycus.Rycus_backend.repository.UserBlockRepository userBlockRepository;
    private final UserService userService;

    public UserController(
            UserRepository userRepository,
            PostRepository postRepository,
            PostImageRepository postImageRepository,
            UserService userService,
            com.rycus.Rycus_backend.repository.UserBlockRepository userBlockRepository
    ) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.postImageRepository = postImageRepository;
        this.userService = userService;
        this.userBlockRepository = userBlockRepository;
    }

    public static class UpdateMeRequest {
        public String fullName;
        public String phone;
        public String avatarUrl;
        public String businessName;
        public String industry;
        public String city;
        public String state;

        public Boolean offersReferralFee;
        public String referralFeeType;
        public BigDecimal referralFeeValue;
        public String referralFeeNotes;
    }

    @GetMapping("/me")
    public ResponseEntity<SafeUserDto> me(Authentication authentication) {

        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        String email = authentication.getName();

        User user = userRepository
                .findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        SafeUserDto dto = SafeUserDto.from(user);

        if (dto != null) {
            if (dto.getPlanType() == null && user.getPlanType() != null) {
                dto.setPlanType(user.getPlanType().name());
            }

            if (dto.getSubscriptionStatus() == null && user.getSubscriptionStatus() != null) {
                dto.setSubscriptionStatus(user.getSubscriptionStatus());
            }
        }

        return ResponseEntity.ok(dto);
    }

    @PutMapping("/me")
    public ResponseEntity<SafeUserDto> updateMe(
            Authentication authentication,
            @RequestBody UpdateMeRequest body
    ) {

        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        String email = authentication.getName();

        User user = userRepository
                .findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (body != null) {
            if (body.fullName != null) user.setFullName(body.fullName.trim());
            if (body.phone != null) user.setPhone(body.phone.trim());
            if (body.avatarUrl != null) user.setAvatarUrl(body.avatarUrl.trim());
            if (body.businessName != null) user.setBusinessName(body.businessName.trim());
            if (body.industry != null) user.setIndustry(body.industry.trim());
            if (body.city != null) user.setCity(body.city.trim());
            if (body.state != null) user.setState(body.state.trim());

            if (body.offersReferralFee != null) {
                user.setOffersReferralFee(body.offersReferralFee);

                if (!body.offersReferralFee) {
                    user.setReferralFeeType(null);
                    user.setReferralFeeValue(null);
                    user.setReferralFeeNotes(null);
                }
            }

            if (body.referralFeeType != null) {
                user.setReferralFeeType(body.referralFeeType.trim());
            }

            if (body.referralFeeValue != null) {
                user.setReferralFeeValue(body.referralFeeValue);
            }

            if (body.referralFeeNotes != null) {
                user.setReferralFeeNotes(body.referralFeeNotes.trim());
            }
        }

        userRepository.save(user);

        return ResponseEntity.ok(SafeUserDto.from(user));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMe(Authentication authentication) {

        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        String email = authentication.getName();

        User user = userRepository
                .findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        userRepository.delete(user);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/by-email")
    public ResponseEntity<SafeUserDto> byEmail(
            @RequestParam("email") String email
    ) {

        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "email is required");
        }

        User user = userRepository
                .findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        return ResponseEntity.ok(SafeUserDto.from(user));
    }

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<UserProfileDto> byId(
            @PathVariable("id") Long id
    ) {
        UserProfileDto profile = userService.getUserProfile(id);
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/{id:\\d+}/photos")
    public ResponseEntity<List<UserPhotoDto>> userPhotos(
            @PathVariable("id") Long id
    ) {

        User user = userRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        String email = user.getEmail();

        List<Post> posts = postRepository.findByAuthorEmailIgnoreCaseOrderByCreatedAtDesc(email);

        if (posts.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        List<Long> postIds = posts.stream()
                .map(Post::getId)
                .toList();

        List<PostImage> images = postImageRepository.findByPostIdIn(postIds);

        List<UserPhotoDto> photos = images.stream()
                .map(img -> {
                    Post post = posts.stream()
                            .filter(p -> p.getId().equals(img.getPostId()))
                            .findFirst()
                            .orElse(null);

                    return new UserPhotoDto(
                            img.getPostId(),
                            img.getImageUrl(),
                            post == null ? null : post.getCreatedAt()
                    );
                })
                .toList();

        return ResponseEntity.ok(photos);
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserMiniDto>> search(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "query", required = false) String query,
            Authentication authentication
    ) {

        String raw = (q != null && !q.isBlank()) ? q : query;

        String term = (raw == null) ? "" : raw.trim();

        if (term.length() < 2) {
            return ResponseEntity.ok(List.of());
        }

        User viewer = getAuthenticatedUserOrNull(authentication);

        List<UserMiniDto> results = userRepository.searchMini(term)
                .stream()
                .filter(u -> canViewerSeeUser(viewer, u.getId()))
                .toList();

        return ResponseEntity.ok(results);
    }

    @GetMapping("/search-referrals/advanced")
    public ResponseEntity<List<UserSearchDto>> searchReferralsAdvanced(
            @RequestParam(required = false) String nameEmail,
            @RequestParam(required = false) String industry,
            @RequestParam(required = false) String location,
            Authentication authentication
    ) {

        String cleanNameEmail = clean(nameEmail);
        String cleanIndustry = clean(industry);
        String cleanLocation = clean(location);

        if (
                cleanNameEmail.isBlank()
                        && cleanIndustry.isBlank()
                        && cleanLocation.isBlank()
        ) {
            return ResponseEntity.ok(List.of());
        }

        User viewer = getAuthenticatedUserOrNull(authentication);

        List<UserSearchDto> results =
                userRepository.searchWithReferralFeeAdvanced(
                                cleanNameEmail,
                                cleanIndustry,
                                cleanLocation
                        )
                        .stream()
                        .filter(u -> canViewerSeeUser(viewer, u.getId()))
                        .toList();

        return ResponseEntity.ok(results);
    }

    @GetMapping("/all")
    public ResponseEntity<List<SafeUserDto>> allUsers() {

        List<SafeUserDto> users = userRepository.findAll()
                .stream()
                .map(SafeUserDto::from)
                .toList();

        return ResponseEntity.ok(users);
    }

    @GetMapping("/me-test")
    public ResponseEntity<String> testDeploy() {
        return ResponseEntity.ok("NEW VERSION DEPLOYED ✅");
    }


    @PostMapping("/block/{id}")
    public ResponseEntity<Void> blockUser(
            @PathVariable("id") Long id,
            Authentication authentication
    ) {
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        User blocker = userRepository
                .findByEmailIgnoreCase(authentication.getName().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Current user not found"));

        User blocked = userRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User to block not found"));

        if (blocker.getId().equals(blocked.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot block yourself");
        }

        boolean alreadyBlocked = userBlockRepository.existsByBlockerAndBlocked(blocker, blocked);

        if (!alreadyBlocked) {
            userBlockRepository.save(new UserBlock(blocker, blocked));
        }

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/block/{id}")
    public ResponseEntity<Void> unblockUser(
            @PathVariable("id") Long id,
            Authentication authentication
    ) {
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        User blocker = userRepository
                .findByEmailIgnoreCase(authentication.getName().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Current user not found"));

        User blocked = userRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User to unblock not found"));

        userBlockRepository
                .findByBlockerAndBlocked(blocker, blocked)
                .ifPresent(userBlockRepository::delete);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/block/{id}")
    public ResponseEntity<Boolean> isUserBlocked(
            @PathVariable("id") Long id,
            Authentication authentication
    ) {
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        User blocker = userRepository
                .findByEmailIgnoreCase(authentication.getName().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Current user not found"));

        User blocked = userRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        boolean blockedStatus = userBlockRepository.existsByBlockerAndBlocked(blocker, blocked);

        return ResponseEntity.ok(blockedStatus);
    }


    private User getAuthenticatedUserOrNull(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return null;
        }

        return userRepository
                .findByEmailIgnoreCase(authentication.getName().trim())
                .orElse(null);
    }

    private boolean canViewerSeeUser(User viewer, Long targetUserId) {
        if (targetUserId == null) {
            return false;
        }

        if (viewer == null || viewer.getId() == null) {
            return true;
        }

        if (Objects.equals(viewer.getId(), targetUserId)) {
            return true;
        }

        User target = userRepository
                .findById(targetUserId)
                .orElse(null);

        if (target == null) {
            return false;
        }

        boolean viewerBlockedTarget =
                userBlockRepository.existsByBlockerAndBlocked(viewer, target);

        boolean targetBlockedViewer =
                userBlockRepository.existsByBlockerAndBlocked(target, viewer);

        return !viewerBlockedTarget && !targetBlockedViewer;
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }
}