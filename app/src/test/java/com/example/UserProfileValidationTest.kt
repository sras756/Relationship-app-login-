package com.example

import com.example.data.local.entity.toDomainModel
import com.example.data.local.entity.toEntity
import com.example.data.model.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UserProfileValidationTest {

    @Test
    fun testEmptyProfileIsNotComplete() {
        val emptyProfile = UserProfile(uid = "test_user_1", email = "test@example.com")
        assertFalse("Empty profile must not be marked as complete", emptyProfile.isProfileComplete)
        assertFalse("Empty profile has not satisfied all required fields", emptyProfile.hasAllRequiredInformation())
    }

    @Test
    fun testPartialProfileValidation() {
        // Missing photos
        val noPhotos = UserProfile(
            uid = "u1",
            displayName = "Alex",
            age = 24,
            gender = "Male",
            interestedIn = listOf("Female"),
            country = "United States",
            relationshipGoal = "Long-term connection 💖",
            bio = "Love traveling and coffee",
            photoUrls = emptyList()
        )
        assertFalse("Profile without photos must fail validation", noPhotos.hasAllRequiredInformation())

        // Age under 18
        val underAge = noPhotos.copy(
            photoUrls = listOf("https://example.com/p1.jpg"),
            age = 17
        )
        assertFalse("Profile with age < 18 must fail validation", underAge.hasAllRequiredInformation())

        // Missing bio
        val noBio = underAge.copy(age = 22, bio = "   ")
        assertFalse("Profile with blank bio must fail validation", noBio.hasAllRequiredInformation())

        // Fully completed profile
        val completeProfile = noBio.copy(bio = "Adventures and coffee lover")
        assertTrue("Fully filled profile must pass validation", completeProfile.hasAllRequiredInformation())
    }

    @Test
    fun testEntityMappersPreserveData() {
        val original = UserProfile(
            uid = "user_999",
            email = "alex@test.com",
            displayName = "Alex Morgan",
            age = 26,
            gender = "Female",
            interestedIn = listOf("Male", "Female"),
            country = "Canada",
            city = "Toronto",
            relationshipGoal = "Long-term connection 💖",
            bio = "Designer & coffee enthusiast",
            photoUrls = listOf("https://example.com/photo.jpg"),
            primaryPhotoUrl = "https://example.com/photo.jpg",
            isVerified = true,
            isProfileComplete = true,
            profileSetupStep = "completed",
            accountStatus = "active"
        )

        val entity = original.toEntity(onboardingStep = 7)
        assertEquals("user_999", entity.uid)
        assertEquals("Alex Morgan", entity.displayName)
        assertEquals(26, entity.age)
        assertEquals("completed", entity.profileSetupStep)
        assertTrue(entity.isProfileComplete)

        val domain = entity.toDomainModel()
        assertEquals(original.uid, domain.uid)
        assertEquals(original.displayName, domain.displayName)
        assertEquals(original.age, domain.age)
        assertEquals(original.bio, domain.bio)
        assertEquals(original.isProfileComplete, domain.isProfileComplete)
        assertEquals(original.accountStatus, domain.accountStatus)
    }
}
