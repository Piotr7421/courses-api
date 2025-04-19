package pl.spring.lessons.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.spring.lessons.common.Language;

import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Builder
public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String firstName;
    private String lastName;

    @Enumerated
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "teacher_language", joinColumns = @JoinColumn(name = "teacher_id"))
    @Column(name = "language")
    private Set<Language> languages;

    @OneToMany(mappedBy = "teacher")
    private Set<Student> students;

    @OneToMany(mappedBy = "teacher")
    private Set<Lesson> lessons;

    @Version
    private int version;

    @Override
    public String toString() {
        return firstName + " " + lastName;
    }
}
