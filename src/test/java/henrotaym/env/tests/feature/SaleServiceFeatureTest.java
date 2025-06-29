package henrotaym.env.tests.feature;

import static org.junit.jupiter.api.Assertions.assertEquals;

import henrotaym.env.ApplicationTest;
import henrotaym.env.database.factories.SaleVegetableFactory;
import henrotaym.env.database.factories.VegetableFactory;
import henrotaym.env.entities.Vegetable;
import henrotaym.env.http.requests.SaleRequest;
import henrotaym.env.http.requests.SaleVegetableRequest;
import henrotaym.env.http.requests.relationships.VegetableRelationshipRequest;
import henrotaym.env.repositories.VegetableRepository;
import henrotaym.env.services.SaleService;
import jakarta.persistence.EntityManager;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class SaleServiceFeatureTest extends ApplicationTest {
  // Ceux là, par contre, c'est moi qui les ai faits :)
  @Autowired private VegetableFactory vegetableFactory;

  @Autowired private SaleVegetableFactory saleVegetableFactory;

  @Autowired private SaleService saleService;

  @Autowired private VegetableRepository vegetableRepository;

  @Autowired EntityManager entityManager;

  @Test
  public void it_Remove_Qauntity_Stock_Of_Vegetable_After_A_Sale() {
    Vegetable vegetable = this.vegetableFactory.create();
    VegetableRelationshipRequest vegetableRelationshipRequest =
        new VegetableRelationshipRequest(vegetable.getId());
    SaleVegetableRequest saleVegetableRequest =
        new SaleVegetableRequest(BigInteger.valueOf(10), vegetableRelationshipRequest);
    List<SaleVegetableRequest> saleVegetableRequests = new ArrayList<SaleVegetableRequest>();
    saleVegetableRequests.add(saleVegetableRequest);

    SaleRequest saleRequest = new SaleRequest(saleVegetableRequests);

    saleService.checkout(saleRequest);

    entityManager.flush();
    entityManager.clear();

    Vegetable newVegetable = this.vegetableRepository.findById(vegetable.getId()).get();

    BigInteger newstock = newVegetable.getStock();

    assertEquals(vegetable.getId(), newVegetable.getId());
    assertEquals(vegetable.getStock().subtract(BigInteger.valueOf(10)), newstock);
  }

  @Test
  public void it_Try_To_Remove_More_Qauntity_Them_The_Stock_Of_Vegetable_After_A_Sale() {
    Vegetable vegetable = this.vegetableFactory.create();
    VegetableRelationshipRequest vegetableRelationshipRequest =
        new VegetableRelationshipRequest(vegetable.getId());
    SaleVegetableRequest saleVegetableRequest =
        new SaleVegetableRequest(BigInteger.valueOf(100000000), vegetableRelationshipRequest);
    List<SaleVegetableRequest> saleVegetableRequests = new ArrayList<SaleVegetableRequest>();
    saleVegetableRequests.add(saleVegetableRequest);

    SaleRequest saleRequest = new SaleRequest(saleVegetableRequests);

    saleService.checkout(saleRequest);

    entityManager.flush();
    entityManager.clear();

    Vegetable newVegetable = this.vegetableRepository.findById(vegetable.getId()).get();

    assertEquals(vegetable.getId(), newVegetable.getId());
    assertEquals(vegetable.getStock(), vegetable.getStock());
  }
}
