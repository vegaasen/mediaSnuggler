package com.gtfo.snuggle.upnp;

import com.gtfo.snuggle.upnp.content.Content;
import org.teleal.cling.support.contentdirectory.AbstractContentDirectoryService;
import org.teleal.cling.support.contentdirectory.ContentDirectoryErrorCode;
import org.teleal.cling.support.contentdirectory.ContentDirectoryException;
import org.teleal.cling.support.contentdirectory.DIDLParser;
import org.teleal.cling.support.model.*;
import org.teleal.cling.support.model.container.Container;
import org.teleal.cling.support.model.item.Item;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class used to browse the content directory.
 * BUG FOUND: Please notice, that if the getRole of an PersonWithRole is set to <null>, then the parsing _WILL_ fail.
 * This will be fixed in the futuristic version of Teleal/Cling framework.
 *
 * @author vegaasen / CLING
 * @since 0.1.a
 */

public class ContentDirectory extends AbstractContentDirectoryService {

    private static final Logger LOGGER = Logger.getLogger(ContentDirectory.class.getName());

    private final Content content;

    public ContentDirectory(Content content) {
        this.content = content;
    }

    public Content getContent() {
        return content;
    }

    @Override
    public BrowseResult browse(String objectID,
                               BrowseFlag browseFlag,
                               String filter,
                               long firstResult,
                               long maxResults,
                               SortCriterion[] sortCriteria) throws ContentDirectoryException {
        try {
            DIDLContent didlContent = new DIDLContent();
            final DIDLObject didlObject;
            int count = 0;
            int totalMatches = 0;

            didlObject = getContent().findObjectWithId(objectID);

            if (didlObject == null) {
                LOGGER.fine("Object not found: " + objectID);
                return new BrowseResult(new DIDLParser().generate(didlContent), 0, 0);
            }

            // TODO: Some filtering, Any sorting
            if (browseFlag.equals(BrowseFlag.METADATA)) {
                if (didlObject instanceof Container) {
                    LOGGER.info("Browsing metadata of container: " + didlObject.getId());
                    didlContent.addContainer((Container) didlObject);
                    count++;
                    totalMatches++;
                } else if (didlObject instanceof Item) {
                    LOGGER.info("Browsing metadata of item: " + didlObject.getId());
                    didlContent.addItem((Item) didlObject);
                    count++;
                    totalMatches++;
                }
            } else if (browseFlag.equals(BrowseFlag.DIRECT_CHILDREN)) {
                if (didlObject instanceof Container) {
                    LOGGER.info("Browsing children of container: " + didlObject.getId());
                    Container container = (Container) didlObject;
                    boolean maxReached = maxResults == 0;
                    totalMatches = totalMatches + container.getContainers().size();

                    for (Container subContainer : container.getContainers()) {
                        if (maxReached){
                            break;
                        }
                        if (firstResult > 0 && count == firstResult){
                            continue;
                        }
                        didlContent.addContainer(subContainer);
                        count++;
                        if (count >= maxResults){
                            maxReached = true;
                        }
                    }

                    totalMatches = totalMatches + container.getItems().size();
                    LOGGER.info("Is MAX reached? If so, just skip the whole next session. maxReached: " + maxReached);

                    for (Item item : container.getItems()) {
                        if (maxReached){
                            break;
                        }
                        if (firstResult > 0 && count == firstResult){
                            continue;
                        }
                        didlContent.addItem(item);
                        count++;
                        if (count >= maxResults){
                            maxReached = true;
                        }
                    }
                }
            }

            LOGGER.info("Browsing result count/max ration: " + count + "/" + maxResults + ", and total matches: " + totalMatches);
            return new BrowseResult(new DIDLParser().generate(didlContent), count, totalMatches);

        } catch (ContentDirectoryException e) {
            e.printStackTrace(System.err);
            throw new ContentDirectoryException(ContentDirectoryErrorCode.CANNOT_PROCESS, e.toString());
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.log(Level.SEVERE, "Exception caught. Please see stack-trace. Most likely this was an NPE.");
            throw new ContentDirectoryException(ContentDirectoryErrorCode.CANNOT_PROCESS, e.toString());
        }
    }
}
