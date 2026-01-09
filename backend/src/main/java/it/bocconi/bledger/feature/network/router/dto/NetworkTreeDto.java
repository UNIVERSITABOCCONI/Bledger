package it.bocconi.bledger.feature.network.router.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class NetworkTreeDto extends NetworkElementDto{
    List<NetworkTreeDto> children;
}
