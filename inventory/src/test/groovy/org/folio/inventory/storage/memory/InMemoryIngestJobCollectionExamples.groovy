package org.folio.inventory.storage.memory

import org.folio.inventory.domain.Item
import org.folio.inventory.resources.ingest.IngestJob
import org.folio.inventory.resources.ingest.IngestJobCollection
import org.folio.inventory.resources.ingest.IngestJobState
import org.folio.inventory.storage.memory.InMemoryIngestJobCollection
import org.junit.Before
import org.junit.Test

import java.util.concurrent.CompletableFuture

import static org.folio.metadata.common.FutureAssistance.complete
import static org.folio.metadata.common.FutureAssistance.getOnCompletion
import static org.folio.metadata.common.FutureAssistance.waitForCompletion

class InMemoryIngestJobCollectionExamples {

  private final IngestJobCollection collection = new InMemoryIngestJobCollection()

  @Before
  public void before() {
    def emptied = new CompletableFuture()

    collection.empty(complete(emptied))

    waitForCompletion(emptied)
  }

  @Test
  void jobsCanBeAdded() {
    def addFuture = new CompletableFuture<IngestJob>()

    collection.add(new IngestJob(IngestJobState.REQUESTED),
      complete(addFuture))

    waitForCompletion(addFuture)

    def findFuture = new CompletableFuture<List<IngestJob>>()

    collection.findAll(complete(findFuture))

    def allJobs = getOnCompletion(findFuture)

    assert allJobs.size() == 1

    assert allJobs.every { it.id != null }
    assert allJobs.every { it.state == IngestJobState.REQUESTED }
  }

  @Test
  void jobsCanBeFoundById() {
    def addFuture = new CompletableFuture<IngestJob>()

    collection.add(new IngestJob(IngestJobState.REQUESTED),
      complete(addFuture))

    def added = getOnCompletion(addFuture)

    def findFuture = new CompletableFuture<IngestJob>()

    collection.findById(added.id, complete(findFuture))

    def found = getOnCompletion(findFuture)

    assert found.id == added.id
    assert found.state == IngestJobState.REQUESTED
  }

  @Test
  void jobStateCanBeUpdated() {
    def addFuture = new CompletableFuture<IngestJob>()

    collection.add(new IngestJob(IngestJobState.REQUESTED),
      complete(addFuture))

    def added = getOnCompletion(addFuture)

    def completed = added.complete()

    def updateFuture = new CompletableFuture<IngestJob>()

    collection.update(completed, complete(updateFuture))

    waitForCompletion(updateFuture)

    def findFuture = new CompletableFuture<IngestJob>()

    collection.findById(added.id, complete(findFuture))

    def found = getOnCompletion(findFuture)

    assert found.state == IngestJobState.COMPLETED
  }

  @Test
  void singleJobWithSameIdFollowingUpdate() {
    def addFuture = new CompletableFuture<IngestJob>()

    collection.add(new IngestJob(IngestJobState.REQUESTED),
      complete(addFuture))

    def added = getOnCompletion(addFuture)

    def completed = added.complete()

    def updateFuture = new CompletableFuture<IngestJob>()

    collection.update(completed, complete(updateFuture))

    waitForCompletion(updateFuture)

    def findAllFuture = new CompletableFuture<List<IngestJob>>()

    collection.findAll(complete(findAllFuture))

    def all = getOnCompletion(findAllFuture)

    assert all.count({ it.id == added.id }) == 1
  }
}
