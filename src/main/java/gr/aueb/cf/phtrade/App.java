package gr.aueb.cf.phtrade;

import gr.aueb.cf.phtrade.core.enums.RoleType;
import gr.aueb.cf.phtrade.model.Pharmacy;
import gr.aueb.cf.phtrade.model.TradeRecord;
import gr.aueb.cf.phtrade.model.User;
import gr.aueb.cf.phtrade.service.util.JPAHelper;
import jakarta.persistence.EntityManager;

import java.time.LocalDateTime;

public class App {

    public static void main(String[] args) {

        EntityManager em = getEntityManager();

        em.getTransaction().begin();

        Pharmacy vas = em.find(Pharmacy.class, 2);
        Pharmacy soutsou = em.find(Pharmacy.class, 1);

        User dimitris = em.find(User.class, 2);

        System.out.print("Βασ Giver: ");
        System.out.println(dimitris.getUsername());
        System.out.println(vas.getRecordsGiver());
        System.out.print("Σουτσου Giver: ");
        System.out.println(soutsou.getName());
        System.out.println(soutsou.getRecordsGiver());

        System.out.print("Δημητρης Pharmacies: ");
        System.out.println(dimitris.getUsername());
        System.out.println(dimitris.getPharmacies());

        System.out.print("Δημητρης Recorder: ");
        System.out.println(dimitris.getUsername());
        System.out.println(dimitris.getRecordsRecorder());

        em.getTransaction().commit();

        em.close();
    }

    public static EntityManager getEntityManager() {
        return JPAHelper.getEntityManager();
    }
}
