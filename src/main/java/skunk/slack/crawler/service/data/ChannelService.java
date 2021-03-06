package skunk.slack.crawler.service.data;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import skunk.slack.crawler.data.dao.spec.ChannelDao;
import skunk.slack.crawler.data.entity.model.Channel;
import skunk.slack.crawler.httpaccess.client.SlackClient;

public class ChannelService {
	private Map<String, Channel> channels = null;
	private SlackClient slackClient;
	private ChannelDao channelDao;

	public ChannelService(SlackClient slackClient, ChannelDao channelDao) {
		this.slackClient = slackClient;
		this.channelDao = channelDao;
	}

	public Set<Channel> getChannels(Predicate<Channel> filter) {
		channels = channelDao.getAll().stream().collect(Collectors.toMap(c -> c.getId(), c -> c));
		return channels.values().stream().filter(filter).collect(Collectors.toSet());

	}

	public Set<Channel> fetchChannels(Predicate<Channel> filter) {
		Map<String, Channel> channels = channelDao.getAll().stream().collect(Collectors.toMap(c -> c.getId(), c -> c));
		List<Channel> fetchedChannels = slackClient.getPublicChannels().getList();
		fetchedChannels.addAll(slackClient.getPrivateChannels().getList());
		fetchedChannels.addAll(slackClient.getDirectMessages().getList());
		return fetchedChannels.stream().map(c -> {
			Channel savedChannel = channels.get(c.getId());
			if (Objects.nonNull(savedChannel)) {
				c.setLastFetchedTs(savedChannel.getLastFetchedTs());
			}
			return c;
		}).filter(filter).collect(Collectors.toSet());
	}

	public Set<Channel> getChannels() {
		return getChannels(c -> true);
	}

	public Channel getChannel(String id) {
		if (Objects.isNull(channels)) {
			channels = channelDao.getAll().stream().collect(Collectors.toMap(c -> c.getId(), c -> c));
		}
		return channels.get(id);
	}
}
