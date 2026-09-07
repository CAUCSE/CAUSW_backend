package net.causw.app.main.domain.admin.emailcampaign.service.implementation;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Component;

import net.causw.app.main.shared.exception.errorcode.EmailCampaignErrorCode;

@Component
public class EmailHtmlSanitizer {

	private static final Set<String> ALLOWED_STYLE_PROPERTIES = Set.of(
		"color", "background-color", "font-size", "font-weight", "font-family", "text-align", "text-decoration",
		"line-height", "margin", "margin-top", "margin-right", "margin-bottom", "margin-left", "padding",
		"padding-top", "padding-right", "padding-bottom", "padding-left", "border", "border-radius", "width",
		"height", "max-width", "display");

	private static final Safelist SAFELIST = new Safelist()
		.addTags("p", "br", "h1", "h2", "h3", "h4", "blockquote", "ul", "ol", "li", "strong", "b", "em", "i",
			"u", "s", "hr", "a", "img", "table", "thead", "tbody", "tr", "th", "td", "span", "div")
		.addAttributes(":all", "style")
		.addAttributes("a", "href", "target", "rel")
		.addAttributes("img", "src", "alt", "width", "height")
		.addAttributes("table", "width", "cellpadding", "cellspacing", "border")
		.addAttributes("td", "colspan", "rowspan", "width")
		.addProtocols("a", "href", "https")
		.addProtocols("img", "src", "https")
		.addEnforcedAttribute("a", "rel", "noopener noreferrer");

	public String sanitize(String html) {
		if (html == null || html.isBlank()) {
			throw EmailCampaignErrorCode.EMAIL_CAMPAIGN_INVALID_HTML.toBaseException();
		}
		Document dirty = Jsoup.parseBodyFragment(html);
		Document clean = new Cleaner(SAFELIST).clean(dirty);
		clean.outputSettings().prettyPrint(false);
		clean.body().getAllElements().forEach(this::sanitizeStyle);
		if (clean.body().text().isBlank() && clean.body().select("img").isEmpty()) {
			throw EmailCampaignErrorCode.EMAIL_CAMPAIGN_INVALID_HTML.toBaseException();
		}
		return clean.body().html();
	}

	private void sanitizeStyle(Element element) {
		if (!element.hasAttr("style")) {
			return;
		}
		String sanitized = Arrays.stream(element.attr("style").split(";"))
			.map(String::trim)
			.filter(declaration -> !declaration.isEmpty())
			.filter(this::isAllowedDeclaration)
			.collect(Collectors.joining("; "));
		if (sanitized.isEmpty()) {
			element.removeAttr("style");
		} else {
			element.attr("style", sanitized);
		}
	}

	private boolean isAllowedDeclaration(String declaration) {
		int separator = declaration.indexOf(':');
		if (separator <= 0) {
			return false;
		}
		String property = declaration.substring(0, separator).trim().toLowerCase(Locale.ROOT);
		String value = declaration.substring(separator + 1).trim().toLowerCase(Locale.ROOT);
		return ALLOWED_STYLE_PROPERTIES.contains(property)
			&& !value.contains("url(")
			&& !value.contains("expression")
			&& !value.contains("javascript:")
			&& !value.contains("@import");
	}
}
