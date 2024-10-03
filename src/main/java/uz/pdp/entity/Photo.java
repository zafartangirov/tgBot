package uz.pdp.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class Photo {
    private Integer albumId;
    private Integer id;
    private String title;
    private String url;
    private String thumbnailUrl;
}
