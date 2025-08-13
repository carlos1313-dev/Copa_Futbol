
package com.pes.copa.matches.dto.external;
import lombok.Data;
/**
 *
 * @author sangr
 */
@Data
public class TeamDTO {
    private Long id;
    private String name;
    private String country;
    private String continent;
    private String logoURL;
    private Boolean isChampions;
}
