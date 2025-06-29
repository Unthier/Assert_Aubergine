package henrotaym.env.tests.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import henrotaym.env.ApplicationTest;
import henrotaym.env.entities.Sale;
import henrotaym.env.entities.Vegetable;
import henrotaym.env.exceptions.InsufficientStockException;
import henrotaym.env.http.requests.SaleRequest;
import henrotaym.env.http.requests.SaleVegetableRequest;
import henrotaym.env.http.requests.relationships.VegetableRelationshipRequest;
import henrotaym.env.repositories.SaleRepository;
import henrotaym.env.repositories.SaleVegetableRepository;
import henrotaym.env.repositories.VegetableRepository;
import henrotaym.env.services.SaleService;
import java.math.BigInteger;
import java.util.List;
import org.junit.jupiter.api.Test;

public class SaleServiceUnitTest extends ApplicationTest {

  // Je ne vais pas vous cacher que ChatGPT m'a pas mal aider pour ce code... j'avais un peu de mal
  // avec les Verify et les When des mock...
  // Si non, le reste c'est bien moi qu'il l'ai fais.
  @Test
  public void it_Request_Sale_When_Stock_Is_Sufficient() {

    VegetableRepository vegetableRepository = mock(VegetableRepository.class);
    SaleRepository saleRepository = mock(SaleRepository.class);
    SaleVegetableRepository saleVegetableRepository = mock(SaleVegetableRepository.class);

    SaleService saleService =
        new SaleService(vegetableRepository, saleRepository, saleVegetableRepository);

    Long vegId = 1L;
    BigInteger quantityRequested = BigInteger.valueOf(10);
    VegetableRelationshipRequest relationshipRequest = new VegetableRelationshipRequest(vegId);
    SaleVegetableRequest saleVegetableRequest =
        new SaleVegetableRequest(quantityRequested, relationshipRequest);
    SaleRequest saleRequest = new SaleRequest(List.of(saleVegetableRequest));

    Vegetable vegetable = new Vegetable();
    vegetable.setId(vegId);
    vegetable.setStock(BigInteger.valueOf(50));
    vegetable.setPrice(BigInteger.valueOf(2));

    when(vegetableRepository.findAllById(List.of(vegId))).thenReturn(List.of(vegetable));
    when(saleRepository.save(any(Sale.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(saleVegetableRepository.saveAll(any())).thenReturn(null);
    when(vegetableRepository.saveAll(any())).thenReturn(null);

    Sale newsale = saleService.checkout(saleRequest);

    verify(vegetableRepository).findAllById(List.of(vegId));
    verify(saleRepository).save(any(Sale.class));
    verify(saleVegetableRepository).saveAll(any());
    verify(vegetableRepository).saveAll(any());

    assertNotNull(newsale);
    assertEquals(BigInteger.valueOf(20), newsale.getAmount());
  }

  @Test
  public void it_Request_Sale_When_Stock_Is_Insufficient() {
    VegetableRepository vegetableRepository = mock(VegetableRepository.class);
    SaleRepository saleRepository = mock(SaleRepository.class);
    SaleVegetableRepository saleVegetableRepository = mock(SaleVegetableRepository.class);

    SaleService saleService =
        new SaleService(vegetableRepository, saleRepository, saleVegetableRepository);

    Long vegId = 1L;
    BigInteger quantityRequested = BigInteger.valueOf(10);
    VegetableRelationshipRequest relationshipRequest = new VegetableRelationshipRequest(vegId);
    SaleVegetableRequest saleVegetableRequest =
        new SaleVegetableRequest(quantityRequested, relationshipRequest);
    SaleRequest saleRequest = new SaleRequest(List.of(saleVegetableRequest));

    Vegetable vegetable = new Vegetable();
    vegetable.setId(vegId);
    vegetable.setStock(BigInteger.valueOf(5));
    vegetable.setPrice(BigInteger.valueOf(2));

    when(vegetableRepository.findAllById(List.of(vegId))).thenReturn(List.of(vegetable));

    assertThrows(InsufficientStockException.class, () -> saleService.checkout(saleRequest));

    verify(saleRepository, never()).save(any());
    verify(vegetableRepository, never()).saveAll(any());
    verify(saleVegetableRepository, never()).saveAll(any());
  }
}
