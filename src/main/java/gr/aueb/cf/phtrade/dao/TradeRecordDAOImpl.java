package gr.aueb.cf.phtrade.dao;

import gr.aueb.cf.phtrade.model.TradeRecord;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TradeRecordDAOImpl extends AbstractDAO<TradeRecord> implements ITradeRecordDAO {

    public TradeRecordDAOImpl(){
        this.setPersistenceClass(TradeRecord.class);
    }
}
