package com.yourssu.spacer.spacehub.storage.domain.meeting

import com.yourssu.spacer.spacehub.implement.domain.meeting.RegularMeeting
import com.yourssu.spacer.spacehub.storage.domain.space.SpaceEntity
import jakarta.persistence.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

@Entity
@Table(name = "regular_meeting")
class RegularMeetingEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "space_id", nullable = false, foreignKey = ForeignKey(name = "fk_regular_meeting_space"))
    val space: SpaceEntity,

    @Column(nullable = false)
    val organizerName: String,

    @Column(nullable = false)
    val teamName: String,

    @Column(nullable = false)
    val startDate: LocalDate,

    @Column(nullable = false)
    val endDate: LocalDate,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val dayOfWeek: DayOfWeek,

    @Column(nullable = false)
    val startTime: LocalTime,

    @Column(nullable = false)
    val endTime: LocalTime
) {
    companion object {
        fun from(regularMeeting: RegularMeeting) = RegularMeetingEntity(
            id = regularMeeting.id,
            space = SpaceEntity.from(regularMeeting.space),
            organizerName = regularMeeting.organizerName,
            teamName = regularMeeting.teamName,
            startDate = regularMeeting.startDate,
            endDate = regularMeeting.endDate,
            dayOfWeek = regularMeeting.dayOfWeek,
            startTime = regularMeeting.startTime,
            endTime = regularMeeting.endTime
        )
    }

    fun toDomain() = RegularMeeting(
        id = id,
        space = space.toDomain(),
        organizerName = organizerName,
        teamName = teamName,
        startDate = startDate,
        endDate = endDate,
        dayOfWeek = dayOfWeek,
        startTime = startTime,
        endTime = endTime
    )
}
