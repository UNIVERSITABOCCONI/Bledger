package it.bocconi.bledger.feature.network.dao.repository.row;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ComputeEPercentageRow {

    private double ratio;
    private long count;
}
