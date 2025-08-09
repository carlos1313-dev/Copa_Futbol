
package com.pes.copa.tournaments.dto.external;
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
    private String logoURL;
    private Boolean isChampions;
}
