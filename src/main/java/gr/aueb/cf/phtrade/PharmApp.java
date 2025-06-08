package gr.aueb.cf.phtrade;

import gr.aueb.cf.phtrade.core.enums.RoleType;
import gr.aueb.cf.phtrade.dao.IUserDAO;
import gr.aueb.cf.phtrade.dao.UserDAOImpl;
import gr.aueb.cf.phtrade.model.User;
import gr.aueb.cf.phtrade.service.UserServiceImpl;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/api")
public class PharmApp extends Application {

//    static {
//        try {
//            System.out.println("Manually initializing JPA...");
//            EntityManagerFactory emf = Persistence.createEntityManagerFactory("phtrade7rest");
//            emf.createEntityManager().close();
//            System.out.println("Done.");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

}

