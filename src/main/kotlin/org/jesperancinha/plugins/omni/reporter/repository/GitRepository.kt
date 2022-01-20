package org.jesperancinha.plugins.omni.reporter.repository

import org.eclipse.jgit.lib.Constants.HEAD
import org.eclipse.jgit.lib.RepositoryBuilder
import org.eclipse.jgit.revwalk.RevWalk
import org.jesperancinha.plugins.omni.reporter.domain.Git
import org.jesperancinha.plugins.omni.reporter.domain.Head
import org.jesperancinha.plugins.omni.reporter.domain.Remote
import org.jesperancinha.plugins.omni.reporter.pipelines.Pipeline
import java.io.File

/**
 * Created by jofisaes on 01/01/2022
 */
class GitRepository(sourceDirectory: File, pipeline: Pipeline) {

    private val repo = RepositoryBuilder().findGitDir(sourceDirectory).build()

    private val head = let {
        val revision = repo.resolve(HEAD)
        val commit = RevWalk(repo).parseCommit(revision)
        Head(
            revision.name,
            commit.authorIdent.name,
            commit.authorIdent.emailAddress,
            commit.committerIdent.name,
            commit.committerIdent.emailAddress,
            commit.fullMessage
        )
    }

    private val branch = pipeline.branchRef ?: repo.branch

    private val remotes = repo.config.getSubsections("remote")
        .map { Remote(it, repo.config.getString("remote", it, "url")) }

    val git = Git(head, branch, remotes)
}