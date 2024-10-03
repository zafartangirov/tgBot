package uz.pdp.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class Album {
    private Integer userId;
    private Integer id;
    private String title;
}
